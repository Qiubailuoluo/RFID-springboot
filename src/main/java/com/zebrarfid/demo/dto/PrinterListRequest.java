package com.zebrarfid.demo.dto;

import lombok.Data;

@Data
public class PrinterListRequest {
    private String keyword; // 搜索关键词（非必填）
    private String type;    // 打印机类型（usb/tcp/network，非必填）
}
