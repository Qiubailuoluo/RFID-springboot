package com.zebrarfid.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zebrarfid.demo.entity.LabelTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签模板 Mapper 接口（MyBatis-Plus）
 * 继承 BaseMapper 获得默认的增删改查方法，自定义方法可在本接口添加并在 LabelTemplateMapper.xml 中实现
 */
@Mapper
public interface LabelTemplateMapper extends BaseMapper<LabelTemplate> {

    // 基础增删改查（insert/deleteById/updateById/selectById/selectList 等）已由 BaseMapper 提供，无需重复编写
    // 若后续需要自定义复杂查询（如按模板名称模糊查询），可在此添加方法声明，例如：
    // List<LabelTemplate> selectByTemplateName(String templateName);
}
