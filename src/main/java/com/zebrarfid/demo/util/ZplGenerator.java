package com.zebrarfid.demo.util;


import com.zebrarfid.demo.dto.label.LabelTemplateSaveRequest;
import java.util.List;

public class ZplGenerator {
    // 根据填充后的模板生成ZPL指令
    public static String generateZpl(LabelTemplateSaveRequest template, int copies) {
        LabelTemplateSaveRequest.TemplateDTO tpl = template.getTemplate();
        LabelTemplateSaveRequest.MetaDTO meta = tpl.getMeta();
        List<LabelTemplateSaveRequest.ElementDTO> elements = tpl.getElements();

        // ZPL头部：设置标签尺寸
        StringBuilder zpl = new StringBuilder("^XA");
        zpl.append("^LL").append(meta.getHeight()); // 标签长度
        zpl.append("^PW").append(meta.getWidth()); // 标签宽度

        // 遍历元素生成ZPL指令
        for (LabelTemplateSaveRequest.ElementDTO element : elements) {
            String type = element.getType();
            int x = element.getPosition().getX();
            int y = element.getPosition().getY();

            switch (type) {
                case "text":
                    generateTextElement(zpl, element, x, y);
                    break;
                case "barcode":
                    generateBarcodeElement(zpl, element, x, y);
                    break;
                // EPC/TID类型后续扩展
                default:
                    break;
            }
        }

        // ZPL尾部：设置打印份数并结束
        zpl.append("^PQ").append(copies); // 打印份数
        zpl.append("^XZ");
        return zpl.toString();
    }

    // 生成文本元素ZPL
    private static void generateTextElement(StringBuilder zpl, LabelTemplateSaveRequest.ElementDTO element, int x, int y) {
        int fontSize = element.getStyle() != null ? element.getStyle().getFontSize() : 20;
        String content = element.getContent();
        // ^FOx,y^A0N,字体高度,字体宽度^FD内容^FS
        zpl.append("^FO").append(x).append(",").append(y);
        zpl.append("^A0N,").append(fontSize).append(",").append(fontSize / 2);
        zpl.append("^FD").append(content).append("^FS");
    }

    // 生成条码元素ZPL
    private static void generateBarcodeElement(StringBuilder zpl, LabelTemplateSaveRequest.ElementDTO element, int x, int y) {
        String barcodeType = element.getBarcodeType();
        String data = element.getData();
        int height = element.getSize().getHeight();
        boolean humanReadable = element.getHumanReadable() != null && element.getHumanReadable();

        zpl.append("^FO").append(x).append(",").append(y);
        // 常用条码类型映射：CODE128→^BC, CODE39→^B3
        String zplCode = "BC";
        if ("CODE39".equals(barcodeType)) {
            zplCode = "B3";
        }
        // ^B类型,高度,是否可读^FD数据^FS
        zpl.append("^").append(zplCode).append(",")
                .append(height).append(",").append(humanReadable ? "Y" : "N");
        zpl.append("^FD").append(data).append("^FS");
    }
}
