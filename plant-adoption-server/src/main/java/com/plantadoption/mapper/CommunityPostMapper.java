package com.plantadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantadoption.entity.CommunityPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 社区帖子Mapper接口
 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
    
    @Update("UPDATE community_post SET view_count = view_count + 1 WHERE id = #{postId}")
    int incrementViewCount(@Param("postId") Long postId);
    
    @Update("UPDATE community_post SET like_count = like_count + #{delta} WHERE id = #{postId}")
    int updateLikeCount(@Param("postId") Long postId, @Param("delta") int delta);
    
    @Select("SELECT COUNT(*) FROM community_post WHERE user_id = #{userId} AND deleted = 0")
    int countByUserId(@Param("userId") Long userId);
}
