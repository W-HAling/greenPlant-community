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
 * 绿植养护计划实例
 */
@Data
@TableName("plant_care_plan")
public class PlantCarePlan implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long plantId;

    private Long templateId;

    private Long adopterId;

    private String adjustNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
