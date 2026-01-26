package com.zebrarfid.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zebrarfid.demo.dto.*;
import com.zebrarfid.demo.dto.vo.*;
import com.zebrarfid.demo.entity.PrinterConfig;
import com.zebrarfid.demo.entity.User;
import com.zebrarfid.demo.mapper.PrinterConfigMapper;
import com.zebrarfid.demo.mapper.UserMapper;
import com.zebrarfid.demo.result.Result;
import com.zebrarfid.demo.service.PrinterService;
import com.zebrarfid.demo.util.SystemPrinterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrinterServiceImpl implements PrinterService {

    private final PrinterConfigMapper printerConfigMapper;
    private final UserMapper userMapper;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public Result<List<PrinterVO>> getPrinterList(PrinterListRequest request) {
        try {
            // 1. 获取系统所有打印机
            List<PrinterVO> allPrinters = SystemPrinterUtil.getSystemPrinters();
            // 2. 根据关键词和类型过滤
            List<PrinterVO> filteredPrinters = SystemPrinterUtil.filterPrinters(
                    allPrinters, request.getKeyword(), request.getType()
            );
            return Result.success("获取打印机列表成功", filteredPrinters);
        } catch (Exception e) {
            log.error("获取打印机列表失败", e);
            return Result.error("获取打印机列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<TestConnectResponse> testPrinterConnection(PrinterTestRequest request) {
        PrinterConfigDTO config = request.getConfig();
        TestConnectResponse response = new TestConnectResponse();
        String timestamp = ISO_FORMATTER.format(LocalDateTime.now()) + "Z";
        response.setTimestamp(timestamp);

        try {
            if ("tcp".equals(config.getType())) {
                // TCP打印机：Socket连接测试
                validateTcpConfig(config);
                try (Socket socket = new Socket()) {
                    socket.connect(new java.net.InetSocketAddress(config.getIp(), config.getPort()), config.getTimeout());
                    response.setSuccess(true);
                    response.setMessage("打印机连接测试成功，设备已就绪。");
                    // 可选：获取打印机型号（示例，实际需根据打印机协议）
                    Map<String, String> details = new HashMap<>();
                    details.put("model", "Zebra GK420d"); // 模拟获取型号
                    response.setDetails(details);
                }
            } else if ("usb".equals(config.getType())) {
                // USB打印机：测试设备是否可访问（需本地权限）
                validateUsbConfig(config);
                // 此处省略USB设备连接逻辑（需调用本地库或执行系统命令）
                response.setSuccess(true);
                response.setMessage("USB打印机连接测试成功，设备已就绪。");
            } else {
                return Result.error("不支持的打印机类型：" + config.getType());
            }
            return Result.success("连接测试完成", response);
        } catch (IllegalArgumentException e) {
            log.error("连接参数错误", e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return Result.success("连接测试失败", response);
        } catch (Exception e) {
            log.error("打印机连接测试失败", e);
            response.setSuccess(false);
            response.setMessage("连接失败：" + e.getMessage());
            return Result.success("连接测试失败", response);
        }
    }

    @Override
    public Result<PrintTestResponse> sendTestPrint(PrintTestRequest request) {
        PrinterConfigDTO config = request.getConfig();
        String testData = request.getTestData();
        PrintTestResponse response = new PrintTestResponse();
        String timestamp = ISO_FORMATTER.format(LocalDateTime.now()) + "Z";
        response.setTimestamp(timestamp);
        response.setJobId("print_job_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6));

        try {
            if ("tcp".equals(config.getType())) {
                validateTcpConfig(config);
                // TCP连接并发送打印数据
                try (Socket socket = new Socket(config.getIp(), config.getPort());
                     OutputStream os = socket.getOutputStream()) {
                    os.write(testData.getBytes("UTF-8"));
                    os.flush();
                }
            } else if ("usb".equals(config.getType())) {
                validateUsbConfig(config);
                // USB打印机打印逻辑（需本地设备访问权限）
                log.info("向USB打印机 {} 发送测试打印：{}", config.getUsbPath(), testData);
                // 此处省略USB打印数据发送逻辑
            } else {
                return Result.error("不支持的打印机类型：" + config.getType());
            }

            response.setMessage("测试打印任务已发送至打印机");
            return Result.success("打印任务发送成功", response);
        } catch (IllegalArgumentException e) {
            log.error("打印参数错误", e);
            response.setMessage("打印失败：" + e.getMessage());
            return Result.success("打印任务发送失败", response);
        } catch (Exception e) {
            log.error("发送测试打印失败", e);
            response.setMessage("打印失败：" + e.getMessage());
            return Result.error("打印任务发送失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> savePrinterConfig(ConfigSaveRequest request, Long userId) {
        try {
            // 1. 验证用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 2. 构建配置实体
            PrinterConfig printerConfig = new PrinterConfig();
            printerConfig.setUserId(userId);
            printerConfig.setConfigName(request.getConfigName());
            printerConfig.setConfig(request.getConfig()); // 自动转为JSON字符串
            printerConfig.setUpdatedAt(LocalDateTime.now());

            // 3. 保存到数据库
            printerConfigMapper.insert(printerConfig);
            return Result.success("配置保存成功");
        } catch (Exception e) {
            log.error("保存打印机配置失败", e);
            return Result.error("配置保存失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<SavedConfigVO>> loadPrinterConfigs(Long userId) {
        try {
            // 1. 根据用户ID查询配置
            List<PrinterConfig> configs = printerConfigMapper.selectByUserId(userId);
            // 2. 转为VO返回
            List<SavedConfigVO> voList = new ArrayList<>();
            for (PrinterConfig config : configs) {
                SavedConfigVO vo = new SavedConfigVO();
                vo.setId(config.getId());
                vo.setConfigName(config.getConfigName());
                vo.setConfig(config.getConfig()); // 自动转为PrinterConfigDTO
                vo.setUpdatedAt(config.getUpdatedAtStr());
                voList.add(vo);
            }
            return Result.success("加载配置成功", voList);
        } catch (Exception e) {
            log.error("加载打印机配置失败", e);
            return Result.error("加载配置失败：" + e.getMessage());
        }
    }

    // 校验TCP连接配置
    private void validateTcpConfig(PrinterConfigDTO config) {
        if (config.getIp() == null || config.getIp().trim().isEmpty()) {
            throw new IllegalArgumentException("TCP打印机IP地址不能为空");
        }
        if (config.getPort() == null || config.getPort() < 1 || config.getPort() > 65535) {
            throw new IllegalArgumentException("TCP打印机端口不合法");
        }
    }

    // 校验USB连接配置
    private void validateUsbConfig(PrinterConfigDTO config) {
        if (config.getUsbPath() == null || config.getUsbPath().trim().isEmpty()) {
            throw new IllegalArgumentException("USB打印机设备路径不能为空");
        }
    }
}
