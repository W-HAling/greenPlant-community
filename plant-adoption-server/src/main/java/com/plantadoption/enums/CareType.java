package com.plantadoption.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 养护类型枚举
 */
@Getter
public enum CareType implements IEnum<String> {
    
    WATER("WATER", "浇水"),
    FERTILIZE("FERTILIZE", "施肥"),
    PRUNE("PRUNE", "修剪"),
    PEST_CONTROL("PEST_CONTROL", "病虫害防治"),
    OTHER("OTHER", "其他");
    
    @EnumValue
    @JsonValue
    private final String value;
    private final String description;
    
    CareType(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    @Override
    public String getValue() {
        return this.value;
    }
}
