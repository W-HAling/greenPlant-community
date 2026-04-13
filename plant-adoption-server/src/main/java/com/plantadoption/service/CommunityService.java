package com.plantadoption.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plantadoption.dto.CommentDTO;
import com.plantadoption.dto.PostDTO;
import com.plantadoption.entity.CommunityComment;
import com.plantadoption.entity.CommunityPost;

/**
 * 社区服务接口
 */
public interface CommunityService extends IService<CommunityPost> {
    
    /**
     * 发布帖子
     */
    CommunityPost createPost(Long userId, PostDTO dto);
    
    /**
     * 更新帖子
     */
    CommunityPost updatePost(Long userId, Long postId, PostDTO dto);
    
    /**
     * 删除帖子
     */
    void deletePost(Long userId, Long postId);
    
    /**
     * 分页查询帖子列表
     */
    IPage<CommunityPost> pagePosts(IPage<CommunityPost> page, String postType, Long userId);
    
    /**
     * 获取帖子详情
     */
    CommunityPost getPostDetail(Long postId, Long currentUserId);
    
    /**
     * 点赞/取消点赞帖子
     */
    boolean toggleLike(Long userId, Long postId);
    
    /**
     * 发表评论
     */
    CommunityComment createComment(Long userId, CommentDTO dto);
    
    /**
     * 删除评论
     */
    void deleteComment(Long userId, Long commentId);
    
    /**
     * 分页查询评论列表
     */
    IPage<CommunityComment> pageComments(IPage<CommunityComment> page, Long postId);
}
