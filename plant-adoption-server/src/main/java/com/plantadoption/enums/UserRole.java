package com.plantadoption.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole implements IEnum<String> {
    
    USER("USER", "普通用户"),
    ADMIN("ADMIN", "管理员");
    
    @EnumValue
    @JsonValue
    private final String value;
    private final String description;
    
    UserRole(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    @Override
    public String getValue() {
        return this.value;
    }
}
