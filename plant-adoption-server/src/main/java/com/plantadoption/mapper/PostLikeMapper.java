package com.plantadoption.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plantadoption.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 帖子点赞Mapper接口
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
    
    @Select("SELECT * FROM post_like WHERE post_id = #{postId} AND user_id = #{userId} AND like_type = #{likeType} AND deleted = 0")
    PostLike selectByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId, @Param("likeType") String likeType);
}
