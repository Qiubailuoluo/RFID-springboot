// ConfigSaveRequest.java（保存配置请求）
package com.zebrarfid.demo.dto.printconnect;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ConfigSaveRequest {
    @NotBlank(message = "配置名称不能为空")
    private String configName; // 配置名称（如"仓库主打印机"）

    @NotNull(message = "连接配置不能为空")
    @Valid
    private PrinterConfigDTO config; // 连接配置
}
