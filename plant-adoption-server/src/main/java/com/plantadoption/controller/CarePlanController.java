package com.plantadoption.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.Result;
import com.plantadoption.dto.CareTaskAdjustDTO;
import com.plantadoption.dto.CareTaskExecuteDTO;
import com.plantadoption.dto.CareTaskStatsDTO;
import com.plantadoption.dto.CarePlanTemplateDTO;
import com.plantadoption.entity.CareLog;
import com.plantadoption.entity.CareTask;
import com.plantadoption.entity.CarePlanTemplate;
import com.plantadoption.service.CarePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "养护计划管理", description = "养护模板相关接口")
@RestController
@RequestMapping("/care-plan")
@RequiredArgsConstructor
public class CarePlanController {

    private final CarePlanService carePlanService;

    @Operation(summary = "新增养护模板")
    @PostMapping("/template")
    public Result<CarePlanTemplate> createTemplate(HttpServletRequest request,
                                                   @Valid @RequestBody CarePlanTemplateDTO dto) {
        Long userId = (Long) request.getAttribute("currentUserId");
        CarePlanTemplate template = carePlanService.createTemplate(dto, userId);
        return Result.success("创建成功", template);
    }

    @Operation(summary = "查询养护模板列表")
    @GetMapping("/template/list")
    public Result<List<CarePlanTemplate>> listTemplates(@RequestParam(required = false) String plantCategory,
                                                        @RequestParam(required = false) Integer status) {
        return Result.success(carePlanService.listTemplates(plantCategory, status));
    }

    @Operation(summary = "绑定绿植养护模板并生成任务")
    @PostMapping("/plant/{plantId}/bind")
    public Result<Void> bindPlantPlan(@PathVariable Long plantId,
                                      @RequestParam Long templateId,
                                      @RequestParam(required = false) String adjustNote) {
        carePlanService.bindPlantPlan(plantId, templateId, adjustNote);
        return Result.success();
    }

    @Operation(summary = "获取我的养护任务")
    @GetMapping("/task/my")
    public Result<List<CareTask>> listMyTasks(HttpServletRequest request,
                                              @RequestParam(required = false) String status) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(carePlanService.listMyTasks(userId, status));
    }

    @Operation(summary = "分页查询养护任务总览")
    @GetMapping("/task/list")
    public Result<IPage<CareTask>> pageTasks(@RequestParam(defaultValue = "1") Integer current,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) String keyword) {
        Page<CareTask> page = new Page<>(current, size);
        return Result.success(carePlanService.pageTasks(page, status, keyword));
    }

    @Operation(summary = "获取养护任务统计")
    @GetMapping("/task/stats")
    public Result<CareTaskStatsDTO> getTaskStats() {
        return Result.success(carePlanService.getTaskStats());
    }

    @Operation(summary = "调整养护任务")
    @PutMapping("/task/{taskId}/adjust")
    public Result<CareTask> adjustTask(@PathVariable Long taskId,
                                       @RequestBody CareTaskAdjustDTO dto) {
        return Result.success("调整成功", carePlanService.adjustTask(taskId, dto));
    }

    @Operation(summary = "执行养护任务")
    @PutMapping("/task/{taskId}/execute")
    public Result<CareLog> executeTask(HttpServletRequest request,
                                       @PathVariable Long taskId,
                                       @RequestBody(required = false) CareTaskExecuteDTO dto) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success("执行成功", carePlanService.executeTask(userId, taskId, dto));
    }
}
