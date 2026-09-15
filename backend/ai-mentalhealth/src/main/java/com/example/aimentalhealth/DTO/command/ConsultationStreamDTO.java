package com.example.aimentalhealth.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsultationStreamDTO {
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;


    @NotBlank(message = "用户消息不能为空")
    @Size(max = 2000, message = "用户消息长度不能超过2000个字符")
    private String userMessage;
}
