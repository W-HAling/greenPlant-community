package com.plantadoption.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 点赞类型枚举
 */
@Getter
public enum LikeType implements IEnum<String> {
    
    POST("POST", "帖子"),
    COMMENT("COMMENT", "评论");
    
    @EnumValue
    @JsonValue
    private final String value;
    private final String description;
    
    LikeType(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    @Override
    public String getValue() {
        return this.value;
    }
}
