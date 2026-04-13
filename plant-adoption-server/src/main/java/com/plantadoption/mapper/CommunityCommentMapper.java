package com.plantadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantadoption.entity.CommunityComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 帖子评论Mapper接口
 */
@Mapper
public interface CommunityCommentMapper extends BaseMapper<CommunityComment> {
    
    @Update("UPDATE community_comment SET like_count = like_count + #{delta} WHERE id = #{commentId}")
    int updateLikeCount(@Param("commentId") Long commentId, @Param("delta") int delta);
}
