// PrintTestRequest.java（发送测试打印请求）
package com.zebrarfid.demo.dto.printconnect;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PrintTestRequest {
    @NotNull(message = "连接配置不能为空")
    @Valid
    private PrinterConfigDTO config; // 连接配置

    @NotBlank(message = "测试打印内容不能为空")
    private String testData;         // 测试打印文本

    // 新增必填字段
    @NotBlank(message = "指令格式不能为空（可选：ZPL/ESC/POS/CPCL）")
    private String commandType;      // 前端选择的指令格式（优先级最高）
}
