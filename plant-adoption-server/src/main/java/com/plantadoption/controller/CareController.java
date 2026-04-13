package com.plantadoption.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.Result;
import com.plantadoption.dto.CareLogDTO;
import com.plantadoption.entity.CareLog;
import com.plantadoption.service.CareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 养护控制器
 */
@Tag(name = "养护管理", description = "养护日志相关接口")
@RestController
@RequestMapping("/care")
@RequiredArgsConstructor
public class CareController {
    
    private final CareService careService;
    
    @Operation(summary = "记录养护日志")
    @PostMapping
    public Result<CareLog> recordCare(HttpServletRequest request, @Valid @RequestBody CareLogDTO dto) {
        Long userId = (Long) request.getAttribute("currentUserId");
        CareLog careLog = careService.recordCare(userId, dto);
        return Result.success("记录成功", careLog);
    }
    
    @Operation(summary = "分页查询养护日志")
    @GetMapping("/list")
    public Result<IPage<CareLog>> pageCareLogs(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String careType) {
        
        Page<CareLog> page = new Page<>(current, size);
        IPage<CareLog> result = careService.pageCareLogs(page, plantId, userId, careType);
        return Result.success(result);
    }
    
    @Operation(summary = "获取养护日志详情")
    @GetMapping("/{id}")
    public Result<CareLog> getCareLogDetail(@PathVariable Long id) {
        CareLog careLog = careService.getCareLogDetail(id);
        return Result.success(careLog);
    }
    
    @Operation(summary = "删除养护日志")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCareLog(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        careService.deleteCareLog(userId, id);
        return Result.success();
    }
    
    @Operation(summary = "获取我的养护日志")
    @GetMapping("/my")
    public Result<IPage<CareLog>> getMyCareLogs(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) String careType) {
        
        Long userId = (Long) request.getAttribute("currentUserId");
        Page<CareLog> page = new Page<>(current, size);
        IPage<CareLog> result = careService.pageCareLogs(page, plantId, userId, careType);
        return Result.success(result);
    }
}
