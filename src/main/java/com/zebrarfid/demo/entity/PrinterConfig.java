package com.zebrarfid.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zebrarfid.demo.util.JsonUtil;
import com.zebrarfid.demo.dto.printconnect.PrinterConfigDTO;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@TableName("printer_config") // 数据库表名
public class PrinterConfig {
    @TableId(type = IdType.AUTO)
    private Long id;                // 主键ID
    private Long userId;            // 关联的用户ID（当前登录用户）
    private String configName;      // 配置名称
    private String configJson;      // 连接配置（JSON字符串存储）
    private LocalDateTime updatedAt;// 更新时间

    // 辅助方法：将PrinterConfigDTO转为configJson
    public void setConfig(PrinterConfigDTO config) {
        this.configJson = JsonUtil.toJson(config);
    }

    // 辅助方法：将configJson转为PrinterConfigDTO
    public PrinterConfigDTO getConfig() {
        return JsonUtil.fromJson(configJson, PrinterConfigDTO.class);
    }

    // 获取ISO格式的更新时间字符串
    public String getUpdatedAtStr() {
        return updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
    }
}
