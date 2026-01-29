package com.zebrarfid.demo.dto.label;

import lombok.Data;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 模板保存请求参数
 * 用于接收前端的请求信息
 */
@Data
public class LabelTemplateSaveRequest {
    // 固定值1.0，必填
    @NotBlank(message = "version不能为空，固定值1.0")
    @Pattern(regexp = "^1.0$", message = "version必须为1.0")
    private String version;

    @NotNull(message = "template不能为空")
    private TemplateDTO template;

    // 模板元数据
    @Data
    public static class TemplateDTO {
        @NotNull(message = "template.meta不能为空")
        private MetaDTO meta;

        @NotEmpty(message = "template.elements不能为空")
        private List<ElementDTO> elements;
    }

    // 元数据DTO
    @Data
    public static class MetaDTO {
        @NotBlank(message = "meta.id不能为空（前端生成UUID）")
        private String id;

        private String name;
        private String description;

        @Min(value = 10, message = "宽度最小10mm")
        @Max(value = 500, message = "宽度最大500mm")
        private Integer width;

        @Min(value = 10, message = "高度最小10mm")
        @Max(value = 500, message = "高度最大500mm")
        private Integer height;

        private Integer dpi;
        private String measurementUnit;
    }

    // 元素DTO
    @Data
    public static class ElementDTO {
        @NotBlank(message = "element.id不能为空")
        private String id;

        @NotBlank(message = "element.type不能为空")
        private String type;

        private String name;

        @NotNull(message = "element.position不能为空")
        private PositionDTO position;

        @NotNull(message = "element.size不能为空")
        private SizeDTO size;

        private Integer zIndex;
        // 文本元素特有字段
        private String content;
        private StyleDTO style;
        // 条码元素特有字段
        private String barcodeType;
        private String data;
        private Boolean humanReadable;
        // 扩展字段
        private Object customData;
    }

    @Data
    public static class PositionDTO {
        private Integer x;
        private Integer y;
    }

    @Data
    public static class SizeDTO {
        private Integer width;
        private Integer height;
    }

    @Data
    public static class StyleDTO {
        private Integer fontSize;
        private String color;
    }
}
