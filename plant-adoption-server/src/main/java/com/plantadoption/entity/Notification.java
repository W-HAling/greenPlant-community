package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.plantadoption.enums.NotificationType;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@TableName("notification")
public class Notification implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title;
    
    private String content;
    
    private NotificationType notificationType;
    
    private Long relatedId;
    
    private String relatedType;
    
    private Integer isRead;
    
    private LocalDateTime readTime;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
