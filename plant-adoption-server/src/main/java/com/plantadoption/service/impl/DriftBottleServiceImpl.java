package com.plantadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantadoption.common.ErrorCode;
import com.plantadoption.dto.DriftBottleReplyDTO;
import com.plantadoption.dto.DriftBottleThrowDTO;
import com.plantadoption.entity.DriftBottle;
import com.plantadoption.entity.DriftBottleLog;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.mapper.DriftBottleLogMapper;
import com.plantadoption.mapper.DriftBottleMapper;
import com.plantadoption.service.DriftBottleService;
import com.plantadoption.service.NotificationService;
import com.plantadoption.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriftBottleServiceImpl implements DriftBottleService {

    private final DriftBottleMapper driftBottleMapper;
    private final DriftBottleLogMapper driftBottleLogMapper;
    private final SysConfigService sysConfigService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public DriftBottle throwBottle(Long senderId, DriftBottleThrowDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getContent())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        int dailyLimit = sysConfigService.getIntConfigValue("drift_bottle_daily_limit", 3);
        int sentCount = driftBottleMapper.countDailySentBySender(senderId);
        if (sentCount >= dailyLimit) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "今日漂流瓶发送次数已达上限");
        }

        DriftBottle bottle = new DriftBottle();
        bottle.setSenderId(senderId);
        bottle.setContent(dto.getContent().trim());
        bottle.setImageUrls(dto.getImageUrls() == null ? null : String.join(",", dto.getImageUrls()));
        bottle.setStatus("FLOATING");
        bottle.setDeleted(0);
        driftBottleMapper.insert(bottle);

        DriftBottleLog log = new DriftBottleLog();
        log.setBottleId(bottle.getId());
        log.setOperatorId(senderId);
        log.setOperation("SEND");
        log.setRemark("发送漂流瓶");
        driftBottleLogMapper.insert(log);

        return bottle;
    }

    @Override
    public List<DriftBottle> listMyBottles(Long userId, String type) {
        LambdaQueryWrapper<DriftBottle> wrapper = new LambdaQueryWrapper<>();
        if ("receive".equalsIgnoreCase(type) || "reply".equalsIgnoreCase(type)) {
            wrapper.eq(DriftBottle::getReceiverId, userId);
        } else {
            wrapper.eq(DriftBottle::getSenderId, userId);
        }
        wrapper.orderByDesc(DriftBottle::getCreateTime);
        return driftBottleMapper.selectList(wrapper);
    }

    @Override
    public IPage<DriftBottle> pageBottles(Page<DriftBottle> page, String status, String keyword) {
        LambdaQueryWrapper<DriftBottle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(status), DriftBottle::getStatus, status.trim());
        wrapper.like(StringUtils.hasText(keyword), DriftBottle::getContent, keyword.trim());
        wrapper.orderByDesc(DriftBottle::getCreateTime);
        return driftBottleMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public DriftBottle pickBottle(Long userId) {
        DriftBottle bottle = driftBottleMapper.selectOne(
                new LambdaQueryWrapper<DriftBottle>()
                        .eq(DriftBottle::getStatus, "FLOATING")
                        .ne(DriftBottle::getSenderId, userId)
                        .orderByAsc(DriftBottle::getCreateTime)
                        .last("limit 1")
        );
        if (bottle == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "暂无可拾取的漂流瓶");
        }

        bottle.setReceiverId(userId);
        bottle.setStatus("PICKED");
        bottle.setPickTime(LocalDateTime.now());
        bottle.setPickExpireTime(LocalDateTime.now().plusHours(24));
        driftBottleMapper.updateById(bottle);

        DriftBottleLog log = new DriftBottleLog();
        log.setBottleId(bottle.getId());
        log.setOperatorId(userId);
        log.setOperation("PICK");
        log.setRemark("拾取漂流瓶");
        driftBottleLogMapper.insert(log);
        return bottle;
    }

    @Override
    @Transactional
    public DriftBottle replyBottle(Long userId, Long bottleId, DriftBottleReplyDTO dto) {
        DriftBottle bottle = driftBottleMapper.selectById(bottleId);
        if (bottle == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "漂流瓶不存在");
        }
        if (!userId.equals(bottle.getReceiverId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅拾取者可回复漂流瓶");
        }
        if (!"PICKED".equalsIgnoreCase(bottle.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前漂流瓶状态不可回复");
        }
        if (dto == null || !StringUtils.hasText(dto.getReplyContent())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        bottle.setReplyContent(dto.getReplyContent().trim());
        bottle.setReplyTime(LocalDateTime.now());
        bottle.setStatus("REPLIED");
        driftBottleMapper.updateById(bottle);

        DriftBottleLog log = new DriftBottleLog();
        log.setBottleId(bottle.getId());
        log.setOperatorId(userId);
        log.setOperation("REPLY");
        log.setRemark("回复漂流瓶");
        driftBottleLogMapper.insert(log);
        notificationService.sendNotification(
                bottle.getSenderId(),
                "漂流瓶收到回复",
                "您投递的漂流瓶已收到新的回复，快去看看吧。",
                "SYSTEM",
                bottle.getId(),
                "DriftBottle"
        );
        return bottle;
    }

    @Override
    @Transactional
    public void releaseBottle(Long userId, Long bottleId) {
        DriftBottle bottle = driftBottleMapper.selectById(bottleId);
        if (bottle == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "漂流瓶不存在");
        }
        if (!userId.equals(bottle.getReceiverId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅拾取者可释放漂流瓶");
        }
        if (!"PICKED".equalsIgnoreCase(bottle.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "当前漂流瓶状态不可释放");
        }

        bottle.setReceiverId(null);
        bottle.setPickTime(null);
        bottle.setPickExpireTime(null);
        bottle.setStatus("FLOATING");
        driftBottleMapper.updateById(bottle);

        DriftBottleLog log = new DriftBottleLog();
        log.setBottleId(bottle.getId());
        log.setOperatorId(userId);
        log.setOperation("RELEASE");
        log.setRemark("释放漂流瓶");
        driftBottleLogMapper.insert(log);
    }
    @Override
    @Transactional
    public void deleteBottle(Long bottleId) {
        DriftBottle bottle = driftBottleMapper.selectById(bottleId);
        if (bottle == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "漂流瓶不存在");
        }
        driftBottleMapper.deleteById(bottleId);
    }
}
