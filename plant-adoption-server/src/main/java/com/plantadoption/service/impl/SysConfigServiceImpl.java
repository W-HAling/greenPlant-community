package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plantadoption.entity.SysConfig;
import com.plantadoption.mapper.SysConfigMapper;
import com.plantadoption.service.SysConfigService;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务实现类
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {
    
    @Override
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }
    
    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        SysConfig config = baseMapper.selectOne(
            new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
        );
        
        return config != null ? config.getConfigValue() : defaultValue;
    }
    
    @Override
    public int getIntConfigValue(String configKey, int defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public boolean getBooleanConfigValue(String configKey, boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value);
    }
    
    @Override
    public void setConfigValue(String configKey, String configValue) {
        SysConfig config = baseMapper.selectOne(
            new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
        );
        
        if (config != null) {
            config.setConfigValue(configValue);
            baseMapper.updateById(config);
        } else {
            config = new SysConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setConfigType("string");
            baseMapper.insert(config);
        }
    }
}
