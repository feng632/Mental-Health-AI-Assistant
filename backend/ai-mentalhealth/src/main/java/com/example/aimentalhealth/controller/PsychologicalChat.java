package com.example.aimentalhealth.controller;

import cn.hutool.json.JSONUtil;
import com.example.aimentalhealth.AiService.PsychologicalSupportService;
import com.example.aimentalhealth.AiService.StructOutput;
import com.example.aimentalhealth.DTO.command.ConsultationSessionCreateDTO;
import com.example.aimentalhealth.DTO.command.ConsultationStreamDTO;
import com.example.aimentalhealth.common.Result;
import com.example.aimentalhealth.exception.BusinessException;
import com.example.aimentalhealth.util.JwtTokenUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChat {
    @Autowired
    private PsychologicalSupportService psychologicalSupportService;

    //开启一次会话（返回会话信息）
    @PostMapping("/session/chat")
    public Result<StructOutput.StreamChatSession> startSession(@Valid @RequestBody ConsultationSessionCreateDTO createDTO) {
        //获取当前用户ID（validateToken 内部已处理字符串→Long 的转换）
        String token = JwtTokenUtil.getCurrentToken();
        JwtTokenUtil.TokenVerificationResult verification = JwtTokenUtil.validateToken(token);
        if (verification == null || !verification.isValid()) {
            throw new BusinessException("token无效");
        }
        Long userId = verification.getUserId();
        StructOutput.StreamChatSession session = psychologicalSupportService.startSession(userId, createDTO);
        return Result.ok(session);
    }

    //流式对话（SSE 逐段返回 AI 回复）
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultationStreamDTO streamDTO) {
        //获取当前用户ID
        String token = JwtTokenUtil.getCurrentToken();
        JwtTokenUtil.TokenVerificationResult verification = JwtTokenUtil.validateToken(token);
        if (verification == null || !verification.isValid()) {
            throw new BusinessException("token无效");
        }
        return psychologicalSupportService.streamPsychologicalChat(streamDTO.getSessionId(), streamDTO.getUserMessage())
                .map(Fragment -> {
                    return ServerSentEvent.<String>builder()
                            .event("message")
                            .data(JSONUtil.toJsonStr(Result.ok(Map.of("content", Fragment,"type","normal"))))
                            .build();
                })
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build()))
        .delayElements(Duration.ofMillis(50));
    }
}
