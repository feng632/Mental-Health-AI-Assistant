package com.example.aimentalhealth.service;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.aimentalhealth.DTO.command.ConsultationSessionCreateDTO;
import com.example.aimentalhealth.entity.ConsultationSession;
import com.example.aimentalhealth.entity.User;
import com.example.aimentalhealth.exception.BusinessException;
import com.example.aimentalhealth.mapper.ConsultationSessionMapper;
import com.example.aimentalhealth.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationSessionService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    public ConsultationSession createSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        //验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        //创建会话记录
        ConsultationSession session = ConsultationSession.builder()
                .userId(userId)
                .sessionTitle(createDTO.getSessionTitle())
                .startedAt(LocalDateTime.now())
                .build();
        //如果未提供标题
        if (StrUtil.isBlank(createDTO.getSessionTitle())) {
            session.setSessionTitle("未命名会话 - " + DateUtil.format(LocalDateTime.now(), "MM-dd HH:mm"));
        }

        //插入记录
        consultationSessionMapper.insert(session);
        return session;
    }
}
