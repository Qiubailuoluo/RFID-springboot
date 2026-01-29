package com.zebrarfid.demo.common.enums;

import lombok.Getter;
/**
 * 元素类型枚举
 * 对应标签模板设计的 组件
 */
@Getter
public enum ElementEnum {
    TEXT("text"),
    BARCODE("barcode"),
    EPC("epc"),
    TID("tid");

    private final String type;

    ElementEnum(String type) {
        this.type = type;
    }

    // 校验元素类型是否合法
    public static boolean isValid(String type) {
        for (ElementEnum e : values()) {
            if (e.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}
