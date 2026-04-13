package com.plantadoption.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
public enum NotificationType implements IEnum<String> {
    
    ADOPTION("ADOPTION", "领养通知"),
    CARE("CARE", "养护通知"),
    SYSTEM("SYSTEM", "系统通知"),
    LIKE("LIKE", "点赞通知"),
    COMMENT("COMMENT", "评论通知");
    
    @EnumValue
    @JsonValue
    private final String value;
    private final String description;
    
    NotificationType(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    @Override
    public String getValue() {
        return this.value;
    }
}
