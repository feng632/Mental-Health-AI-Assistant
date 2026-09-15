package com.example.aimentalhealth.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConsultationSessionCreateDTO {
    @Size( max = 200, message = "会话标题长度必须在1到200个字符之间")
    private String sessionTitle;

    @NotBlank(message = "初始消息不能为空")
    @Size( max = 200, message = "初始消息长度必须在1到200个字符之间")
    private String initialMessage;
}
