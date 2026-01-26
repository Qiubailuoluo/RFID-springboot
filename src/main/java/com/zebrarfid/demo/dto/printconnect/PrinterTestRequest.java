// PrinterTestRequest.java（测试打印机连接请求）
package com.zebrarfid.demo.dto.printconnect;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class PrinterTestRequest {
    @NotNull(message = "连接配置不能为空")
    @Valid
    private PrinterConfigDTO config; // 连接配置
}
