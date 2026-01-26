// 连接配置子DTO（复用）
package com.zebrarfid.demo.dto.printconnect;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class PrinterConfigDTO {
    @NotBlank(message = "打印机类型不能为空（usb/tcp）")
    private String type;        // 类型：usb/tcp
    private String usbPath;     // USB设备路径（type=usb时必填）
    private String ip;          // TCP IP地址（type=tcp时必填）
    private Integer port = 9100;// TCP端口（默认9100）
    private Integer timeout = 5000; // 超时时间（默认5000ms）
}
