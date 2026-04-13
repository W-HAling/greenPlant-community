package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 养护计划模板
 */
@Data
@TableName("care_plan_template")
public class CarePlanTemplate implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateName;

    private String plantCategory;

    private String plantSpecies;

    private String description;

    private Long createBy;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<CarePlanItem> items;
}
