package com.zebrarfid.demo.controller;

import com.zebrarfid.demo.dto.label.LabelTemplateSaveRequest;
import com.zebrarfid.demo.result.label.BaseResponse;

import com.zebrarfid.demo.service.label.LabelTemplateService;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/label/template")
public class LabelTemplateController {

    @Resource
    private LabelTemplateService labelTemplateService;

    @PostMapping("/save")
    public BaseResponse<Map<String, String>> saveTemplate(@Validated @RequestBody LabelTemplateSaveRequest request) {
        String templateId = labelTemplateService.saveTemplate(request);
        Map<String, String> data = new HashMap<>();
        data.put("templateId", templateId);
        return BaseResponse.success("模板保存成功", data);
    }
}
