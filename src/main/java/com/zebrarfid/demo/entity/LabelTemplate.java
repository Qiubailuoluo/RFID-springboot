package com.zebrarfid.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("label_template")
public class LabelTemplate {
    // 主键：对应template.meta.id
    @TableId(type = IdType.INPUT)
    private String id;
    // 版本号：固定1.0
    private String version;
    // 模板JSON：存储完整契约一数据
    private String templateJson;
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;
}
