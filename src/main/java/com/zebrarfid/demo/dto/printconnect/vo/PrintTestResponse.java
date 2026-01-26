// PrintTestResponse.java（打印测试响应）
package com.zebrarfid.demo.dto.printconnect.vo;

import lombok.Data;

@Data
public class PrintTestResponse {
    private String jobId;       // 打印任务ID
    private String message;
    private String timestamp;   // ISO格式时间
}
