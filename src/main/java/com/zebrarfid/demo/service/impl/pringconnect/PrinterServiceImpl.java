package com.zebrarfid.demo.service.impl.pringconnect;

import com.zebrarfid.demo.dto.printconnect.*;
import com.zebrarfid.demo.dto.printconnect.vo.PrintTestResponse;
import com.zebrarfid.demo.dto.printconnect.vo.PrinterVO;
import com.zebrarfid.demo.dto.printconnect.vo.SavedConfigVO;
import com.zebrarfid.demo.dto.printconnect.vo.TestConnectResponse;
import com.zebrarfid.demo.entity.PrinterConfig;
import com.zebrarfid.demo.entity.User;
import com.zebrarfid.demo.mapper.PrinterConfigMapper;
import com.zebrarfid.demo.mapper.UserMapper;
import com.zebrarfid.demo.result.Result;
import com.zebrarfid.demo.service.printconnect.PrinterService;
import com.zebrarfid.demo.util.SystemPrinterUtil;
import jssc.SerialPortException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.zebrarfid.demo.util.UsbPrinterUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrinterServiceImpl implements PrinterService {

    private final PrinterConfigMapper printerConfigMapper;
    private final UserMapper userMapper;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    /**
     * 获取系统打印机列表
     * 塞入 推荐指令格式 zpl等
     * @param request
     * @return
     */
    @Override
    public Result<List<PrinterVO>> getPrinterList(PrinterListRequest request) {
        try {
            String keyword = "";
            String type = "";
            if (request != null) {
                keyword = request.getKeyword() == null ? "" : request.getKeyword().trim();
                type = request.getType() == null ? "" : request.getType().trim();
            }

            List<PrinterVO> allPrinters = SystemPrinterUtil.getSystemPrinters();
            log.info("系统打印机总数：{}", allPrinters.size());

            List<PrinterVO> filteredPrinters = SystemPrinterUtil.filterPrinters(allPrinters, keyword, type);
            log.info("过滤后打印机数：{}", filteredPrinters.size());

            if (filteredPrinters.isEmpty()) {
                log.warn("过滤后无打印机，返回全部系统打印机");
                filteredPrinters = allPrinters;
            }

            return Result.success("获取打印机列表成功", filteredPrinters);
        } catch (Exception e) {
            log.error("获取打印机列表失败", e);
            return Result.success("获取打印机列表成功（兜底数据）", SystemPrinterUtil.getMockPrinters());
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
                validateUsbConfig(config);
                // USB连接测试（用新工具类）
                boolean isConnected = UsbPrinterUtil.testUsbConnection(config.getUsbPath());
                if (isConnected) {
                    response.setSuccess(true);
                    response.setMessage("USB打印机(" + config.getUsbPath() + ")连接成功");
                    // 去掉msg:，直接传字符串+响应对象（Java语法）
                    return Result.success("USB连接测试成功", response);
                } else {
                    response.setSuccess( false);
                    response.setMessage("USB打印机(" + config.getUsbPath() + ")连接失败：端口不可用/无权限");
                    // 去掉code:，直接传字符串+响应对象（Java语法）
                    return Result.error("USB连接测试失败", response);
                }
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
        String commandType = request.getCommandType().toUpperCase(); // 强制大写，避免前端传小写
        PrintTestResponse response = new PrintTestResponse();
        String timestamp = ISO_FORMATTER.format(LocalDateTime.now()) + "Z";
        response.setTimestamp(timestamp);
        response.setJobId("print_job_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6));

        try {
            // 第一步：解析转义字符（如\x1B→0x1B）+ 按指令格式编码
            byte[] printBytes = encodePrintData(testData, commandType);

            // 第二步：按连接类型发送数据
            if ("tcp".equals(config.getType())) {
                validateTcpConfig(config);
                // TCP发送
                try (Socket socket = new Socket(config.getIp(), config.getPort());
                     OutputStream os = socket.getOutputStream()) {
                    os.write(printBytes);
                    os.flush();
                    log.info("TCP打印机({}:{})发送{}格式数据成功，数据长度：{}", config.getIp(), config.getPort(), commandType, printBytes.length);
                }
            } else if ("usb".equals(config.getType())) {
                validateUsbConfig(config);
                // USB发送（用新工具类）
                try {
                    UsbPrinterUtil.sendUsbPrintData(config.getUsbPath(), printBytes);
                    log.info("USB打印机({})发送{}格式数据成功", config.getUsbPath(), commandType);
                } catch (SerialPortException e) {
                    throw new RuntimeException("USB打印失败：" + e.getMessage());
                }
            } else {
                return Result.error("不支持的打印机类型：" + config.getType());
            }

            response.setMessage("测试打印任务已发送至打印机（格式：" + commandType + "）");
            return Result.success("打印任务发送成功", response);
        } catch (IllegalArgumentException e) {
            log.error("打印参数错误", e);
            response.setMessage("打印失败：" + e.getMessage());
            return Result.success("打印任务发送失败", response);
        } catch (RuntimeException e) {
            log.error("USB打印未实现", e);
            response.setMessage("打印失败：" + e.getMessage());
            return Result.error("打印任务发送失败：" + e.getMessage());
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

    // 新增：按指令格式编码打印数据（核心）
    private byte[] encodePrintData(String testData, String commandType) {
        byte[] bytes;
        // 第一步：解析转义字符（如\x1B→0x1B，\n→0x0A，\r→0x0D，\t→0x09）
        String parsedData = parseEscapeChars(testData);

        // 第二步：按指令格式选择编码
        switch (commandType) {
            case "ZPL":
            case "CPCL":
                // ZPL/CPCL：UTF-8编码
                bytes = parsedData.getBytes(Charset.forName("UTF-8"));
                break;
            case "ESC/POS":
                // ESC/POS：小票机90%用GBK编码（避免中文乱码）
                bytes = parsedData.getBytes(Charset.forName("GBK"));
                break;
            default:
                // 未知格式：默认UTF-8
                bytes = parsedData.getBytes(Charset.forName("UTF-8"));
                log.warn("未知指令格式{}，默认UTF-8编码", commandType);
                break;
        }
        return bytes;
    }

    // 新增：解析转义字符（把\\x1B转为0x1B等）
    private String parseEscapeChars(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        // 替换\\xXX为对应字节（如\\x1B→0x1B）
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (input.startsWith("\\x", i) && i + 3 < input.length()) {
                // 提取\x后的两位16进制数
                String hex = input.substring(i + 2, i + 4);
                try {
                    byte b = (byte) Integer.parseInt(hex, 16);
                    sb.append((char) b);
                    i += 4; // 跳过\\xXX
                } catch (NumberFormatException e) {
                    // 解析失败，原样保留
                    sb.append(input.charAt(i));
                    i++;
                }
            } else if (input.charAt(i) == '\\' && i + 1 < input.length()) {
                // 处理常见转义字符
                switch (input.charAt(i + 1)) {
                    case 'n':
                        sb.append('\n');
                        i += 2;
                        break;
                    case 'r':
                        sb.append('\r');
                        i += 2;
                        break;
                    case 't':
                        sb.append('\t');
                        i += 2;
                        break;
                    case '\\':
                        sb.append('\\');
                        i += 2;
                        break;
                    default:
                        sb.append(input.charAt(i));
                        i++;
                        break;
                }
            } else {
                sb.append(input.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
}
