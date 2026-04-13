package com.plantadoption.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.Result;
import com.plantadoption.dto.DriftBottleReplyDTO;
import com.plantadoption.dto.DriftBottleThrowDTO;
import com.plantadoption.entity.DriftBottle;
import com.plantadoption.service.DriftBottleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "漂流瓶管理", description = "漂流瓶相关接口")
@RestController
@RequestMapping("/drift-bottle")
@RequiredArgsConstructor
public class DriftBottleController {

    private final DriftBottleService driftBottleService;

    @Operation(summary = "发送漂流瓶")
    @PostMapping
    public Result<DriftBottle> throwBottle(HttpServletRequest request,
                                           @Valid @RequestBody DriftBottleThrowDTO dto) {
        Long userId = (Long) request.getAttribute("currentUserId");
        DriftBottle bottle = driftBottleService.throwBottle(userId, dto);
        return Result.success("发送成功", bottle);
    }

    @Operation(summary = "我的漂流瓶列表")
    @GetMapping("/my")
    public Result<List<DriftBottle>> listMyBottles(HttpServletRequest request,
                                                   @RequestParam(defaultValue = "send") String type) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(driftBottleService.listMyBottles(userId, type));
    }

    @Operation(summary = "分页查询漂流瓶")
    @GetMapping("/list")
    public Result<IPage<DriftBottle>> pageBottles(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String keyword) {
        Page<DriftBottle> page = new Page<>(current, size);
        return Result.success(driftBottleService.pageBottles(page, status, keyword));
    }

    @Operation(summary = "拾取漂流瓶")
    @PostMapping("/pick")
    public Result<DriftBottle> pickBottle(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(driftBottleService.pickBottle(userId));
    }

    @Operation(summary = "回复漂流瓶")
    @PostMapping("/{id}/reply")
    public Result<DriftBottle> replyBottle(HttpServletRequest request,
                                           @PathVariable Long id,
                                           @RequestBody DriftBottleReplyDTO dto) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(driftBottleService.replyBottle(userId, id, dto));
    }

    @Operation(summary = "释放漂流瓶")
    @PostMapping("/{id}/release")
    public Result<Void> releaseBottle(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        driftBottleService.releaseBottle(userId, id);
        return Result.success();
    }

    @Operation(summary = "删除漂流瓶")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBottle(@PathVariable Long id) {
        driftBottleService.deleteBottle(id);
        return Result.success();
    }
}
