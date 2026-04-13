package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plantadoption.common.ErrorCode;
import com.plantadoption.dto.CommentDTO;
import com.plantadoption.dto.PostDTO;
import com.plantadoption.entity.CommunityComment;
import com.plantadoption.entity.CommunityPost;
import com.plantadoption.entity.PostLike;
import com.plantadoption.entity.User;
import com.plantadoption.enums.LikeType;
import com.plantadoption.enums.PostType;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.mapper.CommunityCommentMapper;
import com.plantadoption.mapper.CommunityPostMapper;
import com.plantadoption.mapper.PostLikeMapper;
import com.plantadoption.mapper.UserMapper;
import com.plantadoption.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 社区服务实现类
 */
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements CommunityService {
    
    private final UserMapper userMapper;
    private final CommunityCommentMapper commentMapper;
    private final PostLikeMapper postLikeMapper;
    
    @Override
    public CommunityPost createPost(Long userId, PostDTO dto) {
        User user = userMapper.selectById(userId);
        
        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setUserName(user.getNickname());
        post.setUserAvatar(user.getAvatar());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImages(dto.getImages());
        post.setPostType(dto.getPostType() != null ? PostType.valueOf(dto.getPostType()) : PostType.SHARE);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setStatus(1);
        
        baseMapper.insert(post);
        return post;
    }
    
    @Override
    public CommunityPost updatePost(Long userId, Long postId, PostDTO dto) {
        CommunityPost post = baseMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImages(dto.getImages());
        if (dto.getPostType() != null) {
            post.setPostType(PostType.valueOf(dto.getPostType()));
        }
        
        baseMapper.updateById(post);
        return post;
    }
    
    @Override
    public void deletePost(Long userId, Long postId) {
        CommunityPost post = baseMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        baseMapper.deleteById(postId);
    }
    
    @Override
    public IPage<CommunityPost> pagePosts(IPage<CommunityPost> page, String postType, Long userId) {
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        
        if (postType != null && !postType.isEmpty()) {
            wrapper.eq(CommunityPost::getPostType, PostType.valueOf(postType));
        }
        if (userId != null) {
            wrapper.eq(CommunityPost::getUserId, userId);
        }
        
        wrapper.eq(CommunityPost::getStatus, 1);
        wrapper.orderByDesc(CommunityPost::getIsTop);
        wrapper.orderByDesc(CommunityPost::getCreateTime);
        
        return baseMapper.selectPage(page, wrapper);
    }
    
    @Override
    public CommunityPost getPostDetail(Long postId, Long currentUserId) {
        CommunityPost post = baseMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        
        baseMapper.incrementViewCount(postId);
        post.setViewCount(post.getViewCount() + 1);
        
        if (currentUserId != null) {
            PostLike like = postLikeMapper.selectByPostIdAndUserId(postId, currentUserId, LikeType.POST.getValue());
            post.setIsLiked(like != null);
        }
        
        return post;
    }
    
    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        CommunityPost post = baseMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        
        PostLike existingLike = postLikeMapper.selectByPostIdAndUserId(postId, userId, LikeType.POST.getValue());
        
        if (existingLike != null) {
            postLikeMapper.deleteById(existingLike.getId());
            baseMapper.updateLikeCount(postId, -1);
            return false;
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setLikeType(LikeType.POST);
            postLikeMapper.insert(like);
            
            baseMapper.updateLikeCount(postId, 1);
            return true;
        }
    }
    
    @Override
    public CommunityComment createComment(Long userId, CommentDTO dto) {
        CommunityPost post = baseMapper.selectById(dto.getPostId());
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        
        User user = userMapper.selectById(userId);
        
        CommunityComment comment = new CommunityComment();
        comment.setPostId(dto.getPostId());
        comment.setUserId(userId);
        comment.setUserName(user.getNickname());
        comment.setUserAvatar(user.getAvatar());
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyUserId(dto.getReplyUserId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        
        commentMapper.insert(comment);
        return comment;
    }
    
    @Override
    public void deleteComment(Long userId, Long commentId) {
        CommunityComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        commentMapper.deleteById(commentId);
    }
    
    @Override
    public IPage<CommunityComment> pageComments(IPage<CommunityComment> page, Long postId) {
        LambdaQueryWrapper<CommunityComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityComment::getPostId, postId);
        wrapper.eq(CommunityComment::getStatus, 1);
        wrapper.orderByAsc(CommunityComment::getCreateTime);
        
        return commentMapper.selectPage(page, wrapper);
    }
}
