package com.plantadoption.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 绿植状态枚举
 */
@Getter
public enum PlantStatus implements IEnum<String> {
    
    AVAILABLE("AVAILABLE", "可领养"),
    ADOPTED("ADOPTED", "已领养"),
    MAINTENANCE("MAINTENANCE", "维护中");
    
    @EnumValue
    @JsonValue
    private final String value;
    private final String description;
    
    PlantStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    @Override
    public String getValue() {
        return this.value;
    }
}
