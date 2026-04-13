package com.plantadoption.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.Result;
import com.plantadoption.dto.PlantDTO;
import com.plantadoption.entity.Plant;
import com.plantadoption.service.PlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 绿植控制器
 */
@Tag(name = "绿植管理", description = "绿植信息相关接口")
@RestController
@RequestMapping("/plant")
@RequiredArgsConstructor
public class PlantController {
    
    private final PlantService plantService;
    
    @Operation(summary = "分页查询绿植列表")
    @GetMapping("/list")
    public Result<IPage<Plant>> pagePlants(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        
        Page<Plant> page = new Page<>(current, size);
        IPage<Plant> result = plantService.pagePlants(page, status, keyword, location);
        return Result.success(result);
    }
    
    @Operation(summary = "获取绿植详情")
    @GetMapping("/{id}")
    public Result<Plant> getPlantDetail(@PathVariable Long id) {
        Plant plant = plantService.getPlantDetail(id);
        return Result.success(plant);
    }
    
    @Operation(summary = "创建绿植")
    @PostMapping
    public Result<Plant> createPlant(@Valid @RequestBody PlantDTO dto) {
        Plant plant = plantService.createPlant(dto);
        return Result.success("创建成功", plant);
    }
    
    @Operation(summary = "更新绿植")
    @PutMapping("/{id}")
    public Result<Plant> updatePlant(@PathVariable Long id, @Valid @RequestBody PlantDTO dto) {
        Plant plant = plantService.updatePlant(id, dto);
        return Result.success("更新成功", plant);
    }
    
    @Operation(summary = "删除绿植")
    @DeleteMapping("/{id}")
    public Result<Void> deletePlant(@PathVariable Long id) {
        plantService.deletePlant(id);
        return Result.success();
    }
    
    @Operation(summary = "生成绿植二维码")
    @PostMapping("/{id}/qrcode")
    public Result<String> generateQrCode(@PathVariable Long id) {
        String qrCode = plantService.generateQrCode(id);
        return Result.success(qrCode);
    }
    
    @Operation(summary = "获取绿植统计数据")
    @GetMapping("/stats")
    public Result<PlantService.PlantStats> getStats() {
        PlantService.PlantStats stats = plantService.getStats();
        return Result.success(stats);
    }
}
