package com.plantadoption.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.plantadoption.common.ErrorCode;
import com.plantadoption.dto.PlantDTO;
import com.plantadoption.entity.Plant;
import com.plantadoption.enums.PlantStatus;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.mapper.PlantMapper;
import com.plantadoption.service.PlantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 绿植服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Override
    public IPage<Plant> pagePlants(IPage<Plant> page, String status, String keyword, String location) {
        LambdaQueryWrapper<Plant> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Plant::getStatus, PlantStatus.valueOf(status));
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(Plant::getName, keyword)
                .or()
                .like(Plant::getVariety, keyword)
                .or()
                .like(Plant::getLocation, keyword)
            );
        }
        if (location != null && !location.isEmpty()) {
            wrapper.like(Plant::getLocation, location);
        }
        
        wrapper.orderByDesc(Plant::getCreateTime);
        
        return baseMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Plant getPlantDetail(Long id) {
        Plant plant = baseMapper.selectPlantWithAdopter(id);
        if (plant == null) {
            throw new BusinessException(ErrorCode.PLANT_NOT_FOUND);
        }
        return plant;
    }
    
    @Override
    public Plant createPlant(PlantDTO dto) {
        Plant plant = new Plant();
        plant.setName(dto.getName());
        plant.setVariety(dto.getVariety());
        plant.setLocation(dto.getLocation());
        plant.setImageUrl(dto.getImageUrl());
        plant.setDescription(dto.getDescription());
        plant.setCareTips(dto.getCareTips());
        plant.setCarePlanTemplateId(dto.getCarePlanTemplateId());
        plant.setStatus(PlantStatus.AVAILABLE);
        plant.setCareCount(0);
        
        baseMapper.insert(plant);
        
        String qrCodeUrl = generateQrCode(plant.getId());
        plant.setQrCodeUrl(qrCodeUrl);
        baseMapper.updateById(plant);
        
        return plant;
    }
    
    @Override
    public Plant updatePlant(Long id, PlantDTO dto) {
        Plant plant = baseMapper.selectById(id);
        if (plant == null) {
            throw new BusinessException(ErrorCode.PLANT_NOT_FOUND);
        }
        
        plant.setName(dto.getName());
        plant.setVariety(dto.getVariety());
        plant.setLocation(dto.getLocation());
        plant.setImageUrl(dto.getImageUrl());
        plant.setDescription(dto.getDescription());
        plant.setCareTips(dto.getCareTips());
        plant.setCarePlanTemplateId(dto.getCarePlanTemplateId());
        
        baseMapper.updateById(plant);
        return plant;
    }
    
    @Override
    public void deletePlant(Long id) {
        Plant plant = baseMapper.selectById(id);
        if (plant == null) {
            throw new BusinessException(ErrorCode.PLANT_NOT_FOUND);
        }
        baseMapper.deleteById(id);
    }
    
    @Override
    public String generateQrCode(Long id) {
        try {
            String qrContent = "plant:" + IdUtil.simpleUUID();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            
            var bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200, hints);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            return "data:image/png;base64," + base64Image;
            
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            return null;
        }
    }
    
    @Override
    public PlantStats getStats() {
        Long totalLong = baseMapper.selectCount(
            new LambdaQueryWrapper<Plant>()
        );
        int total = totalLong != null ? totalLong.intValue() : 0;
        int available = baseMapper.countByStatus(PlantStatus.AVAILABLE.getValue());
        int adopted = baseMapper.countByStatus(PlantStatus.ADOPTED.getValue());
        int maintenance = baseMapper.countByStatus(PlantStatus.MAINTENANCE.getValue());
        
        return new PlantStats(total, available, adopted, maintenance);
    }
}
