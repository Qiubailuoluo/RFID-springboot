// PrinterVO.java（打印机列表项）
package com.zebrarfid.demo.dto.pringconnect;

import lombok.Data;

@Data
public class PrinterVO {
    private String id;          // 后端生成唯一标识（如IP+port/usbPath）
    private String name;        // 打印机名称
    private String type;        // usb/tcp/network
    private String status;      // online/offline
    private String address;     // USB物理地址（type=usb时存在）
    private String ip;          // TCP IP地址（type=tcp时存在）
    private Integer port;       // TCP端口（type=tcp时存在）
    private String description; // 描述
}
