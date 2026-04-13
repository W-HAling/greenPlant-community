package com.plantadoption.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.Result;
import com.plantadoption.dto.CommentDTO;
import com.plantadoption.dto.PostDTO;
import com.plantadoption.entity.CommunityComment;
import com.plantadoption.entity.CommunityPost;
import com.plantadoption.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 社区控制器
 */
@Tag(name = "社区管理", description = "帖子、评论、点赞相关接口")
@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {
    
    private final CommunityService communityService;
    
    @Operation(summary = "发布帖子")
    @PostMapping("/post")
    public Result<CommunityPost> createPost(HttpServletRequest request, @Valid @RequestBody PostDTO dto) {
        Long userId = (Long) request.getAttribute("currentUserId");
        CommunityPost post = communityService.createPost(userId, dto);
        return Result.success("发布成功", post);
    }
    
    @Operation(summary = "更新帖子")
    @PutMapping("/post/{postId}")
    public Result<CommunityPost> updatePost(HttpServletRequest request, @PathVariable Long postId, @Valid @RequestBody PostDTO dto) {
        Long userId = (Long) request.getAttribute("currentUserId");
        CommunityPost post = communityService.updatePost(userId, postId, dto);
        return Result.success("更新成功", post);
    }
    
    @Operation(summary = "删除帖子")
    @DeleteMapping("/post/{postId}")
    public Result<Void> deletePost(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        communityService.deletePost(userId, postId);
        return Result.success();
    }
    
    @Operation(summary = "分页查询帖子列表")
    @GetMapping("/post/list")
    public Result<IPage<CommunityPost>> pagePosts(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String postType,
            @RequestParam(required = false) Long userId) {
        
        Page<CommunityPost> page = new Page<>(current, size);
        IPage<CommunityPost> result = communityService.pagePosts(page, postType, userId);
        return Result.success(result);
    }
    
    @Operation(summary = "获取帖子详情")
    @GetMapping("/post/{postId}")
    public Result<CommunityPost> getPostDetail(HttpServletRequest request, @PathVariable Long postId) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        CommunityPost post = communityService.getPostDetail(postId, currentUserId);
        return Result.success(post);
    }
    
    @Operation(summary = "点赞/取消点赞帖子")
    @PostMapping("/post/{postId}/like")
    public Result<Boolean> toggleLike(HttpServletRequest request, @PathVariable Long postId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        boolean isLiked = communityService.toggleLike(userId, postId);
        return Result.success(isLiked ? "点赞成功" : "取消点赞", isLiked);
    }
    
    @Operation(summary = "发表评论")
    @PostMapping("/comment")
    public Result<CommunityComment> createComment(HttpServletRequest request, @Valid @RequestBody CommentDTO dto) {
        Long userId = (Long) request.getAttribute("currentUserId");
        CommunityComment comment = communityService.createComment(userId, dto);
        return Result.success("评论成功", comment);
    }
    
    @Operation(summary = "删除评论")
    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(HttpServletRequest request, @PathVariable Long commentId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        communityService.deleteComment(userId, commentId);
        return Result.success();
    }
    
    @Operation(summary = "分页查询评论列表")
    @GetMapping("/comment/list")
    public Result<IPage<CommunityComment>> pageComments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long postId) {
        
        Page<CommunityComment> page = new Page<>(current, size);
        IPage<CommunityComment> result = communityService.pageComments(page, postId);
        return Result.success(result);
    }
    
    @Operation(summary = "获取我的帖子")
    @GetMapping("/post/my")
    public Result<IPage<CommunityPost>> getMyPosts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String postType) {
        
        Long userId = (Long) request.getAttribute("currentUserId");
        Page<CommunityPost> page = new Page<>(current, size);
        IPage<CommunityPost> result = communityService.pagePosts(page, postType, userId);
        return Result.success(result);
    }
}
