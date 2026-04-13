package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子评论实体类
 */
@Data
@TableName("community_comment")
public class CommunityComment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long postId;
    
    private Long userId;
    
    private String userName;
    
    private String userAvatar;
    
    private Long parentId;
    
    private Long replyUserId;
    
    private String replyUserName;
    
    private String content;
    
    private Integer likeCount;
    
    private Integer status;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private Boolean isLiked;
}
