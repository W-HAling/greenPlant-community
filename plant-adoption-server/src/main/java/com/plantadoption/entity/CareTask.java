package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 养护任务
 */
@Data
@TableName("care_task")
public class CareTask implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long plantCarePlanId;

    private Long planItemId;

    private Long plantId;

    private Long adopterId;

    private LocalDate dueDate;

    private String status;

    private LocalDateTime lastRemindTime;

    private String cycleTypeOverride;

    private Integer cycleValueOverride;

    private String careDetailOverride;

    private Long careLogId;

    private LocalDateTime completedTime;

    @TableField(exist = false)
    private String plantName;

    @TableField(exist = false)
    private String adopterName;

    @TableField(exist = false)
    private String careType;

    @TableField(exist = false)
    private String careDetail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
