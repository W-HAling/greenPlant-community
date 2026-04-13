package com.plantadoption.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 领养记录状态枚举
 */
@Getter
public enum AdoptionStatus implements IEnum<String> {
    
    PENDING("PENDING", "待审批"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝"),
    CANCELLED("CANCELLED", "已取消"),
    RETURNED("RETURNED", "已归还");
    
    @EnumValue
    @JsonValue
    private final String value;
    private final String description;
    
    AdoptionStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    @Override
    public String getValue() {
        return this.value;
    }
}
