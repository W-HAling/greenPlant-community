package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 养护计划模板项
 */
@Data
@TableName("care_plan_item")
public class CarePlanItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private String careType;

    private String cycleType;

    private Integer cycleValue;

    private String careDetail;

    private String careNote;

    private Integer remindAdvance;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
