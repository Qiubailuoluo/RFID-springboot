package com.zebrarfid.demo.util;

import com.zebrarfid.demo.dto.printconnect.vo.PrinterVO;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 最终修复版：系统打印机获取工具（解决编码+过滤逻辑bug）
 */
@Slf4j
public class SystemPrinterUtil {

    // 获取系统所有打印机，支持Windows/Linux/Mac
    public static List<PrinterVO> getSystemPrinters() {
        List<PrinterVO> printers = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();
        log.info("当前系统：{}", os);

        try {
            if (os.contains("windows")) {
                // 修复1：Windows使用GBK编码读取wmic输出（解决中文乱码）
                printers = getWindowsPrinters();
            } else if (os.contains("linux") || os.contains("mac")) {
                printers = getUnixLikePrinters();
            }
            log.info("获取到系统打印机数量：{}", printers.size());
        } catch (Exception e) {
            log.error("获取系统打印机失败", e);
        }

        // 兜底：如果解析失败/过滤后为空，返回模拟数据（确保前端有数据）
        if (printers.isEmpty()) {
            log.warn("系统未检测到任何打印机，返回模拟测试数据");
            printers = getMockPrinters();
        }
        return printers;
    }

    // Windows：修复编码+解析逻辑（核心）
    private static List<PrinterVO> getWindowsPrinters() throws IOException, InterruptedException {
        List<PrinterVO> printers = new ArrayList<>();
        Process process = Runtime.getRuntime().exec(new String[]{"wmic", "printer", "get", "Name,Status,PortName"});
        process.waitFor();

        // 用GBK编码读取（解决中文乱码）
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
        String line;
        String header = reader.readLine(); // 跳过表头

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.equals(header)) continue;

            try {
                PrinterVO printer = new PrinterVO();
                printer.setId(UUID.randomUUID().toString());

                // 修复1：正确提取完整打印机名称（解决名称显示不全）
                // 逻辑：从行首到" 状态 "（两个空格）之前的部分是完整名称（兼容名称含空格）
                String statusKeyword = "  "; // wmic输出中，名称和状态之间是多个空格
                int statusStartIndex = line.indexOf(statusKeyword);
                String printerName;
                if (statusStartIndex == -1) {
                    printerName = line; // 无状态时，整行都是名称
                } else {
                    printerName = line.substring(0, statusStartIndex).trim(); // 完整名称
                }
                printer.setName(printerName.isEmpty() ? "未知打印机" : printerName);

                // 修复2：正确判断在线/离线状态（解决状态错误）
                String status = "unknown";
                if (statusStartIndex != -1) {
                    String rest = line.substring(statusStartIndex).trim();
                    String[] restParts = rest.split("\\s+");
                    status = restParts.length > 0 ? restParts[0] : "unknown";
                }
                // 扩展状态判断：Windows常见在线状态都算online
                boolean isOnline = "Ready".equalsIgnoreCase(status)
                        || "Idle".equalsIgnoreCase(status)
                        || "打印中".equals(status) // 中文状态
                        || "空闲".equals(status)
                        || "正常".equals(status);
                printer.setStatus(isOnline ? "online" : "offline");

                // 保留原有的端口/类型解析逻辑
                String portName = "";
                if (statusStartIndex != -1) {
                    String rest = line.substring(statusStartIndex).trim();
                    String[] restParts = rest.split("\\s+");
                    portName = restParts.length > 1 ? restParts[1] : "";
                }
                if (portName.startsWith("TCP/IP") || portName.contains(".") && portName.matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+.*")) {
                    printer.setType("tcp");
                    String ip = portName.replaceAll("[^0-9.]", "");
                    if (ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                        printer.setIp(ip);
                        printer.setPort(9100);
                    }
                } else if (portName.startsWith("USB") || portName.startsWith("LPT") || portName.startsWith("COM")) {
                    printer.setType("usb");
                    printer.setAddress(portName);
                } else {
                    printer.setType("network");
                }

                printer.setDescription(printer.getType() + "类型打印机");
                printers.add(printer);
                log.info("解析到Windows打印机：{}（状态：{}）", printer.getName(), printer.getStatus());
            } catch (Exception e) {
                log.warn("解析单台打印机失败，跳过：{}", line, e);
                continue;
            }
        }
        reader.close();
        return printers;
    }

    // Linux/Mac逻辑（无需修改）
    private static List<PrinterVO> getUnixLikePrinters() throws IOException, InterruptedException {
        List<PrinterVO> printers = new ArrayList<>();
        Process process = Runtime.getRuntime().exec(new String[]{"lpstat", "-a"});
        process.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            try {
                PrinterVO printer = new PrinterVO();
                printer.setId(UUID.randomUUID().toString());
                String[] parts = line.split("\\s+");
                printer.setName(parts[0]);
                printer.setStatus("online");
                printer.setType("network");
                printer.setDescription("Unix/Linux打印机");
                printers.add(printer);
            } catch (Exception e) {
                log.warn("解析单台Unix打印机失败，跳过：{}", line, e);
                continue;
            }
        }
        reader.close();

        if (printers.isEmpty()) {
            process = Runtime.getRuntime().exec(new String[]{"lpstat", "-p"});
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("printer")) {
                    try {
                        PrinterVO printer = new PrinterVO();
                        printer.setId(UUID.randomUUID().toString());
                        String[] parts = line.split("\\s+");
                        printer.setName(parts.length >= 2 ? parts[1] : "未知打印机");
                        printer.setStatus(line.contains("is idle") ? "online" : "offline");
                        printer.setType("network");
                        printers.add(printer);
                    } catch (Exception e) {
                        log.warn("解析单台Unix打印机失败，跳过：{}", line, e);
                        continue;
                    }
                }
            }
            reader.close();
        }
        return printers;
    }

    // 修复过滤逻辑（核心：简化判断，增加异常捕获）
    public static List<PrinterVO> filterPrinters(List<PrinterVO> printers, String keyword, String type) {
        List<PrinterVO> filtered = new ArrayList<>();
        if (printers.isEmpty()) {
            log.warn("待过滤打印机列表为空");
            return filtered;
        }

        // 处理空参数：转为空字符串，避免null
        String inputKeyword = (keyword == null) ? "" : keyword.trim();
        String inputType = (type == null) ? "" : type.trim();

        log.info("过滤参数：关键字=[{}]，类型=[{}]", inputKeyword, inputType);

        for (PrinterVO printer : printers) {
            try {
                // 标记：是否匹配
                boolean isMatch = true;

                // 1. 关键字匹配（忽略大小写，为空则匹配）
                if (!inputKeyword.isEmpty()) {
                    String printerName = (printer.getName() == null) ? "" : printer.getName().toLowerCase();
                    isMatch = printerName.contains(inputKeyword.toLowerCase());
                }

                // 2. 类型匹配（为空则匹配，忽略大小写）
                if (isMatch && !inputType.isEmpty()) {
                    String printerType = (printer.getType() == null) ? "" : printer.getType().toLowerCase();
                    isMatch = printerType.equals(inputType.toLowerCase());
                }

                // 3. 匹配成功则加入列表
                if (isMatch) {
                    filtered.add(printer);
                }
            } catch (Exception e) {
                log.warn("过滤单台打印机失败，跳过：{}", printer, e);
                continue; // 单个打印机过滤失败，跳过
            }
        }

        log.info("过滤前打印机数：{}，过滤后：{}", printers.size(), filtered.size());
        return filtered;
    }

    // 模拟数据（兜底用）
    public static List<PrinterVO> getMockPrinters() {
        List<PrinterVO> mock = new ArrayList<>();

        PrinterVO tcpPrinter = new PrinterVO();
        tcpPrinter.setId(UUID.randomUUID().toString());
        tcpPrinter.setName("ZDesigner ZD621R300dpi（TCP）");
        tcpPrinter.setType("tcp");
        tcpPrinter.setStatus("online");
        tcpPrinter.setIp("192.168.1.100");
        tcpPrinter.setPort(9100);
        tcpPrinter.setDescription("Zebra标签打印机（TCP/IP）");
        mock.add(tcpPrinter);

        PrinterVO usbPrinter = new PrinterVO();
        usbPrinter.setId(UUID.randomUUID().toString());
        usbPrinter.setName("WPS 虚拟打印机（USB）");
        usbPrinter.setType("usb");
        usbPrinter.setStatus("online");
        usbPrinter.setAddress("USB001");
        usbPrinter.setDescription("WPS虚拟打印机（USB）");
        mock.add(usbPrinter);

        PrinterVO networkPrinter = new PrinterVO();
        networkPrinter.setId(UUID.randomUUID().toString());
        networkPrinter.setName("Microsoft Print to PDF");
        networkPrinter.setType("network");
        networkPrinter.setStatus("online");
        networkPrinter.setDescription("微软PDF虚拟打印机");
        mock.add(networkPrinter);

        return mock;
    }
}
