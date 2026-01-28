package com.zebrarfid.demo.util;

import lombok.extern.slf4j.Slf4j;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPort;

/**
 * 极简版USB打印工具类（适配io.github.java-native:jssc:2.9.2）
 * 直接使用前端传的USB路径/串口路径，不做复杂适配
 */
@Slf4j
public class UsbPrinterUtil {

    // 核心：向USB打印机发送数据（前端传什么路径就用什么）
    public static void sendUsbPrintData(String usbPortPath, byte[] data) throws SerialPortException {
        // 1. 校验路径
        if (usbPortPath == null || usbPortPath.isEmpty()) {
            throw new IllegalArgumentException("USB打印机路径不能为空！");
        }

        // 2. 打印可用端口（方便你排查路径是否正确）
        String[] availablePorts = SerialPortList.getPortNames();
        log.info("系统可用USB/串口列表：{}", String.join(",", availablePorts));

        // 3. 检查路径是否在可用列表中
        boolean portExist = false;
        for (String port : availablePorts) {
            if (port.equalsIgnoreCase(usbPortPath)) {
                portExist = true;
                break;
            }
        }
        if (!portExist) {
            log.warn("USB路径{}不在可用列表中，尝试强制连接...", usbPortPath);
        }

        // 4. 连接并发送数据（波特率默认9600，大部分小票机通用）
        SerialPort serialPort = new SerialPort(usbPortPath);
        try {
            // 打开端口
            serialPort.openPort();
            // 设置端口参数（波特率9600，8数据位，1停止位，无校验）
            serialPort.setParams(
                    SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );
            // 关闭流控
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            // 发送数据
            serialPort.writeBytes(data);
            log.info("USB打印机({})发送数据成功，数据长度：{}字节", usbPortPath, data.length);

            // 延迟500ms关闭（避免数据截断）
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.warn("USB端口延迟关闭被中断", e);
        } finally {
            // 确保端口关闭
            if (serialPort.isOpened()) {
                serialPort.closePort();
                log.info("USB端口{}已关闭", usbPortPath);
            }
        }
    }

    // 测试USB连接（仅检查端口能否打开）
    public static boolean testUsbConnection(String usbPortPath) {
        if (usbPortPath == null || usbPortPath.isEmpty()) {
            return false;
        }
        SerialPort serialPort = new SerialPort(usbPortPath);
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            return true;
        } catch (SerialPortException e) {
            log.error("USB端口{}连接失败：{}", usbPortPath, e.getMessage());
            return false;
        } finally {
            if (serialPort.isOpened()) {
                try {
                    serialPort.closePort();
                } catch (SerialPortException e) {
                    log.warn("关闭USB端口{}失败", usbPortPath);
                }
            }
        }
    }
}
