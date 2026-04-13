package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.plantadoption.enums.AdoptionStatus;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领养记录实体类
 */
@Data
@TableName("adoption_record")
public class AdoptionRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long plantId;
    
    private String plantName;
    
    private Long userId;
    
    private String userName;
    
    private AdoptionStatus status;
    
    private LocalDateTime applyTime;
    
    private LocalDateTime approveTime;
    
    private Long approverId;
    
    private String approveRemark;
    
    private LocalDateTime returnTime;
    
    private String returnReason;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String userPhone;
    
    @TableField(exist = false)
    private String userAvatar;
    
    @TableField(exist = false)
    private String plantImageUrl;
    
    @TableField(exist = false)
    private String plantLocation;
    
    @TableField(exist = false)
    private String approverName;
}
