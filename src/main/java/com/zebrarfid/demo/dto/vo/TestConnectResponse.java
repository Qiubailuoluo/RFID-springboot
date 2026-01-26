// TestConnectResponse.java（连接测试响应）
package com.zebrarfid.demo.dto.vo;

import lombok.Data;

import java.util.Map;

@Data
public class TestConnectResponse {
    private boolean success;
    private String timestamp;   // ISO格式时间（如2024-01-25T10:30:00Z）
    private String message;
    private Map<String, String> details; // 可选详细信息（如型号）
}
