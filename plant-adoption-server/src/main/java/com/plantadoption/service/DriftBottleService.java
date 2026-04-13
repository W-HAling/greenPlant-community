package com.plantadoption.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.dto.DriftBottleReplyDTO;
import com.plantadoption.dto.DriftBottleThrowDTO;
import com.plantadoption.entity.DriftBottle;

import java.util.List;

public interface DriftBottleService {

    DriftBottle throwBottle(Long senderId, DriftBottleThrowDTO dto);

    List<DriftBottle> listMyBottles(Long userId, String type);

    IPage<DriftBottle> pageBottles(Page<DriftBottle> page, String status, String keyword);

    DriftBottle pickBottle(Long userId);

    DriftBottle replyBottle(Long userId, Long bottleId, DriftBottleReplyDTO dto);

    void releaseBottle(Long userId, Long bottleId);

    void deleteBottle(Long bottleId);
}
