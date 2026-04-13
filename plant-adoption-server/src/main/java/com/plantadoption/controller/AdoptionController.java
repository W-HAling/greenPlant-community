package com.plantadoption.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.Result;
import com.plantadoption.dto.AdoptionApplyDTO;
import com.plantadoption.dto.AdoptionApproveDTO;
import com.plantadoption.entity.AdoptionRecord;
import com.plantadoption.service.AdoptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 领养控制器
 */
@Tag(name = "领养管理", description = "领养申请、审批相关接口")
@RestController
@RequestMapping("/adoption")
@RequiredArgsConstructor
public class AdoptionController {
    
    private final AdoptionService adoptionService;
    
    @Operation(summary = "申请领养")
    @PostMapping("/apply")
    public Result<AdoptionRecord> apply(HttpServletRequest request, @Valid @RequestBody AdoptionApplyDTO dto) {
        Long userId = (Long) request.getAttribute("currentUserId");
        AdoptionRecord record = adoptionService.apply(userId, dto);
        return Result.success("申请成功", record);
    }
    
    @Operation(summary = "审批领养申请")
    @PostMapping("/approve")
    public Result<AdoptionRecord> approve(HttpServletRequest request, @Valid @RequestBody AdoptionApproveDTO dto) {
        Long approverId = (Long) request.getAttribute("currentUserId");
        AdoptionRecord record = adoptionService.approve(approverId, dto);
        return Result.success("审批成功", record);
    }
    
    @Operation(summary = "取消领养申请")
    @PostMapping("/cancel/{recordId}")
    public Result<Void> cancel(HttpServletRequest request, @PathVariable Long recordId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        adoptionService.cancel(userId, recordId);
        return Result.success();
    }
    
    @Operation(summary = "归还绿植")
    @PostMapping("/return/{recordId}")
    public Result<Void> returnPlant(HttpServletRequest request, @PathVariable Long recordId, @RequestParam(required = false) String reason) {
        Long userId = (Long) request.getAttribute("currentUserId");
        adoptionService.returnPlant(userId, recordId, reason);
        return Result.success();
    }
    
    @Operation(summary = "分页查询领养记录")
    @GetMapping("/list")
    public Result<IPage<AdoptionRecord>> pageRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        
        Page<AdoptionRecord> page = new Page<>(current, size);
        IPage<AdoptionRecord> result = adoptionService.pageRecords(page, userId, status);
        return Result.success(result);
    }
    
    @Operation(summary = "获取我的领养记录")
    @GetMapping("/my")
    public Result<IPage<AdoptionRecord>> getMyRecords(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        
        Long userId = (Long) request.getAttribute("currentUserId");
        Page<AdoptionRecord> page = new Page<>(current, size);
        IPage<AdoptionRecord> result = adoptionService.pageRecords(page, userId, status);
        return Result.success(result);
    }
    
    @Operation(summary = "检查是否可以领养")
    @GetMapping("/can-adopt")
    public Result<Boolean> canAdopt(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        boolean result = adoptionService.canAdopt(userId);
        return Result.success(result);
    }
    
    @Operation(summary = "获取领养记录详情")
    @GetMapping("/{id}")
    public Result<AdoptionRecord> getRecordDetail(@PathVariable Long id) {
        AdoptionRecord record = adoptionService.getRecordDetail(id);
        return Result.success(record);
    }
}
