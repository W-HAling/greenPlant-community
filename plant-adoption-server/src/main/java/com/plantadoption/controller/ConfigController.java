package com.plantadoption.controller;

import com.plantadoption.common.Result;
import com.plantadoption.entity.SysConfig;
import com.plantadoption.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@Tag(name = "系统配置", description = "系统配置相关接口")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {
    
    private final SysConfigService sysConfigService;
    
    @Operation(summary = "获取配置值")
    @GetMapping("/{key}")
    public Result<String> getConfigValue(@PathVariable String key) {
        String value = sysConfigService.getConfigValue(key);
        return Result.success(value);
    }
    
    @Operation(summary = "设置配置值")
    @PutMapping("/{key}")
    public Result<Void> setConfigValue(@PathVariable String key, @RequestBody String value) {
        sysConfigService.setConfigValue(key, value);
        return Result.success();
    }
    
    @Operation(summary = "获取所有配置")
    @GetMapping("/list")
    public Result<List<SysConfig>> getAllConfigs() {
        List<SysConfig> configs = sysConfigService.list();
        return Result.success(configs);
    }
    
    @Operation(summary = "获取客户端配置")
    @GetMapping("/client")
    public Result<Map<String, Object>> getClientConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("maxPlantsPerUser", sysConfigService.getIntConfigValue("max_plants_per_user", 3));
        configs.put("careReminderDays", sysConfigService.getIntConfigValue("care_reminder_days", 7));
        configs.put("systemNotice", sysConfigService.getConfigValue("system_notice", ""));
        return Result.success(configs);
    }
}
