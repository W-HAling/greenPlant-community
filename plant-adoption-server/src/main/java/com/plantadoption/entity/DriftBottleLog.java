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
 * 漂流瓶操作日志
 */
@Data
@TableName("drift_bottle_log")
public class DriftBottleLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bottleId;

    private Long operatorId;

    private String operation;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
