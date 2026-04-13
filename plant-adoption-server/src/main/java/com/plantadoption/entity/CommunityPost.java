package com.plantadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.plantadoption.enums.PostType;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社区帖子实体类
 */
@Data
@TableName("community_post")
public class CommunityPost implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String userName;
    
    private String userAvatar;
    
    private String title;
    
    private String content;
    
    private String images;
    
    private PostType postType;
    
    private Integer viewCount;
    
    private Integer likeCount;
    
    private Integer commentCount;
    
    private Integer isTop;
    
    private Integer isEssence;
    
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
