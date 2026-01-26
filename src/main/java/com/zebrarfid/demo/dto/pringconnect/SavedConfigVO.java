// SavedConfigVO.java（保存的配置VO）
package com.zebrarfid.demo.dto.pringconnect;

import com.zebrarfid.demo.dto.PrinterConfigDTO;
import lombok.Data;

@Data
public class SavedConfigVO {
    private Long id;
    private String configName;
    private PrinterConfigDTO config;
    private String updatedAt;   // ISO格式时间
}
