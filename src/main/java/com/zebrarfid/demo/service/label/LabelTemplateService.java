package com.zebrarfid.demo.service.label;


import com.zebrarfid.demo.dto.label.LabelTemplateSaveRequest;
import com.zebrarfid.demo.entity.LabelTemplate;

/**
 * 标签模板 服务层接口
 * 定义模板相关的业务方法，具体实现在 LabelTemplateServiceImpl 中
 */
public interface LabelTemplateService {

    /**
     * 保存/更新标签模板（存在则更新，不存在则新增）
     * @param request 标签模板保存请求DTO（契约一）
     * @return 模板ID（前端传入的 meta.id）
     */
    String saveTemplate(LabelTemplateSaveRequest request);

    /**
     * 根据模板ID查询模板详情
     * @param templateId 模板ID（UUID）
     * @return 标签模板保存请求DTO（契约一格式，便于后续生成ZPL）
     */
    LabelTemplateSaveRequest getTemplateById(String templateId);
}
