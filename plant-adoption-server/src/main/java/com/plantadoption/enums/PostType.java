package com.plantadoption.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 帖子类型枚举
 */
@Getter
public enum PostType implements IEnum<String> {
    
    SHARE("SHARE", "分享"),
    QUESTION("QUESTION", "问答"),
    EXPERIENCE("EXPERIENCE", "经验");
    
    @EnumValue
    @JsonValue
    private final String value;
    private final String description;
    
    PostType(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    @Override
    public String getValue() {
        return this.value;
    }
}
