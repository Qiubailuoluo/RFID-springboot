package com.zebrarfid.demo.util;

import com.zebrarfid.demo.dto.pringconnect.PrinterVO;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class SystemPrinterUtil {

    // 获取系统所有打印机，支持Windows/Linux/Mac
    public static List<PrinterVO> getSystemPrinters() {
        List<PrinterVO> printers = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("windows")) {
                // Windows系统：执行wmic命令
                printers = getWindowsPrinters();
            } else if (os.contains("linux") || os.contains("mac")) {
                // Linux/Mac系统：执行lpstat命令
                printers = getUnixLikePrinters();
            }
        } catch (IOException | InterruptedException e) {
            log.error("获取系统打印机失败", e);
        }

        return printers;
    }

    // Windows：wmic printer get Name,Status,PortName
    private static List<PrinterVO> getWindowsPrinters() throws IOException, InterruptedException {
        List<PrinterVO> printers = new ArrayList<>();
        Process process = Runtime.getRuntime().exec("wmic printer get Name,Status,PortName /format:list");
        process.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
        String line;
        PrinterVO tempPrinter = null;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("Name=")) {
                tempPrinter = new PrinterVO();
                String name = line.split("=", 2)[1];
                tempPrinter.setName(name);
                tempPrinter.setId(UUID.randomUUID().toString()); // 生成唯一ID
                tempPrinter.setStatus("unknown");
            } else if (line.startsWith("Status=") && tempPrinter != null) {
                String status = line.split("=", 2)[1];
                tempPrinter.setStatus("Ready".equals(status) ? "online" : "offline");
            } else if (line.startsWith("PortName=") && tempPrinter != null) {
                String port = line.split("=", 2)[1];
                if (port.startsWith("TCP/IP")) {
                    // TCP打印机：解析IP和端口（如TCP/IP_192.168.1.100_9100）
                    tempPrinter.setType("tcp");
                    String ip = port.replace("TCP/IP_", "").split("_")[0];
                    tempPrinter.setIp(ip);
                    tempPrinter.setPort(9100); // 默认端口，可根据实际解析
                    tempPrinter.setDescription("TCP网络打印机");
                } else if (port.startsWith("USB") || port.startsWith("LPT") || port.startsWith("COM")) {
                    // USB打印机
                    tempPrinter.setType("usb");
                    tempPrinter.setAddress(port);
                    tempPrinter.setDescription("USB本地打印机");
                } else {
                    tempPrinter.setType("network");
                    tempPrinter.setDescription("其他类型打印机");
                }
                printers.add(tempPrinter);
                tempPrinter = null;
            }
        }
        reader.close();
        return printers;
    }

    // Linux/Mac：lpstat -p -d
    private static List<PrinterVO> getUnixLikePrinters() throws IOException, InterruptedException {
        List<PrinterVO> printers = new ArrayList<>();
        Process process = Runtime.getRuntime().exec("lpstat -p");
        process.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("printer")) {
                PrinterVO printer = new PrinterVO();
                String[] parts = line.split("\\s+", 4);
                String name = parts[1];
                String status = parts[2].equals("is") ? "online" : "offline";

                printer.setId(UUID.randomUUID().toString());
                printer.setName(name);
                printer.setStatus(status);

                // 简单判断类型（实际可根据地址解析）
                if (name.contains("TCP") || name.contains("IP")) {
                    printer.setType("tcp");
                    printer.setIp("192.168.1.xxx"); // 实际需解析，这里占位
                    printer.setPort(9100);
                    printer.setDescription("TCP网络打印机");
                } else {
                    printer.setType("usb");
                    printer.setAddress("/dev/usb/lp0"); // 默认USB路径，可根据实际调整
                    printer.setDescription("USB本地打印机");
                }
                printers.add(printer);
            }
        }
        reader.close();
        return printers;
    }

    // 根据关键词和类型过滤打印机
    public static List<PrinterVO> filterPrinters(List<PrinterVO> printers, String keyword, String type) {
        List<PrinterVO> filtered = new ArrayList<>();
        for (PrinterVO printer : printers) {
            // 关键词过滤（名称包含关键词）
            boolean keywordMatch = (keyword == null || keyword.isEmpty())
                    || printer.getName().toLowerCase().contains(keyword.toLowerCase());
            // 类型过滤
            boolean typeMatch = (type == null || type.isEmpty())
                    || printer.getType().equals(type);

            if (keywordMatch && typeMatch) {
                filtered.add(printer);
            }
        }
        return filtered;
    }
}
