package com.zebrarfid.demo.service.impl.label;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zebrarfid.demo.dto.label.LabelTemplateSaveRequest;
import com.zebrarfid.demo.entity.LabelTemplate;
import com.zebrarfid.demo.mapper.LabelTemplateMapper;
import com.zebrarfid.demo.service.label.LabelTemplateService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class LabelTemplateServiceImpl implements LabelTemplateService {

    @Resource
    private LabelTemplateMapper labelTemplateMapper;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String saveTemplate(LabelTemplateSaveRequest request) {
        try {
            String templateId = request.getTemplate().getMeta().getId();
            LabelTemplate template = new LabelTemplate();
            template.setId(templateId);
            template.setVersion(request.getVersion());
            // 将请求DTO转为JSON字符串存储
            template.setTemplateJson(objectMapper.writeValueAsString(request));
            template.setCreateTime(LocalDateTime.now());
            template.setUpdateTime(LocalDateTime.now());

            // 存在则更新，不存在则新增
            LambdaQueryWrapper<LabelTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LabelTemplate::getId, templateId);
            if (labelTemplateMapper.selectCount(wrapper) > 0) {
                labelTemplateMapper.updateById(template);
            } else {
                labelTemplateMapper.insert(template);
            }
            return templateId;
        } catch (Exception e) {
            throw new RuntimeException("模板保存失败：" + e.getMessage());
        }
    }

    @Override
    public LabelTemplateSaveRequest getTemplateById(String templateId) {
        LabelTemplate template = labelTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在：" + templateId);
        }
        try {
            // 将JSON字符串转回DTO
            return objectMapper.readValue(template.getTemplateJson(), LabelTemplateSaveRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("模板解析失败：" + e.getMessage());
        }
    }
}
