package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.plantadoption.enums.LikeType;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子点赞实体类
 */
@Data
@TableName("post_like")
public class PostLike implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long postId;
    
    private Long commentId;
    
    private Long userId;
    
    private LikeType likeType;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
