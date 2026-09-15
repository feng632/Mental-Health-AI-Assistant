package com.example.aimentalhealth.AiService;

import com.example.aimentalhealth.DTO.command.ConsultationSessionCreateDTO;
import com.example.aimentalhealth.DTO.response.ConsultationMessageResponseDTO;
import com.example.aimentalhealth.entity.ConsultationSession;
import com.example.aimentalhealth.service.ConsultationMessageService;
import com.example.aimentalhealth.service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
public class PsychologicalSupportService {
    private static final String SESSION_ID_PREFIX = "session_";
    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ConsultationSessionService consultationSessionService;
    @Autowired
    private ConsultationMessageService consultationMessageService;

    public StructOutput.StreamChatSession startSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        //创建数据库会话记录
        ConsultationSession session = consultationSessionService.createSession(userId, createDTO);

        //将初始用户消息保存到message表
        consultationMessageService.saveUserMessage(session.getId(), createDTO.getInitialMessage(), null);

        //创建会话信息
        String sessionId = SESSION_ID_PREFIX + session.getId();
        return new StructOutput.StreamChatSession(
                sessionId,
                userId,
                createDTO.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 84600000L,
                1,
                "Active");
    }

    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage) {
        //创建响应流
        return Flux.create(sink ->{
            Long dbSessionId = extractSessionId(sessionId);
            if(dbSessionId == null) {
                sink.error(new IllegalArgumentException("Invalid session ID"));
                return;
            }
            boolean isInitialMessage = false;

            Integer messageCount = consultationMessageService.getMessageCount(dbSessionId);
            if(messageCount == 1){
                ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(dbSessionId);
                if(lastMessage != null && lastMessage.getSenderType() == 1 && userMessage.equals(lastMessage.getContent())){
                    isInitialMessage = true;
                }
            }

            if(!isInitialMessage){
                consultationMessageService.saveUserMessage(dbSessionId, userMessage, null);
            }

            //创建流式对话
            //生成对话记忆管理器
            String conversationId = "conversation_"+sessionId;
            //构建系统提示词
            List<Message> userMessages = new ArrayList<>();
            userMessages.add(new UserMessage(userMessage));
            chatMemory.add(conversationId, userMessages);
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
            ));

            //用于存储AI完整的响应内容
            StringBuilder aiResponse = new StringBuilder();
            chatClient.prompt(prompt)
                    .user(userMessage)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content()
                    .doOnNext(Fragment ->{
                            aiResponse.append(Fragment);
                            sink.next(Fragment);
                    })
                    .doOnComplete(()-> {
                        String completeResponse = aiResponse.toString();
                        consultationMessageService.saveAiMessage(dbSessionId, completeResponse, "openai");
                        //添加AI回复导chatMemory
                        List<Message> aiMessages = new ArrayList<>();
                        aiMessages.add(new AssistantMessage(completeResponse));
                        chatMemory.add(conversationId, aiMessages);
                        sink.complete();
                                           })
                    .doOnError(error -> {
                        sink.error(error);
                    })
                    .subscribe();




        });
    }


    public Long extractSessionId(String sessionId) {
        if(sessionId != null && sessionId.startsWith(SESSION_ID_PREFIX)) {
            return Long.parseLong(sessionId.substring(SESSION_ID_PREFIX.length()));
        }
        return null;
    }
}
