package com.example.aimentalhealth.controller;


import com.example.aimentalhealth.DTO.command.UserLoginCommandDTO;
import com.example.aimentalhealth.DTO.command.UserRegisterCommandDTO;
import com.example.aimentalhealth.DTO.response.UserLoginResponseDTO;
import com.example.aimentalhealth.common.Result;
import com.example.aimentalhealth.exception.BusinessException;
import com.example.aimentalhealth.service.UserService;
import com.example.aimentalhealth.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class User {
    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO commandDTO) {
        System.out.println(commandDTO.getUsername());
        System.out.println(commandDTO.getPassword());
        //调用服务层方法
        UserLoginResponseDTO result = userService.login(commandDTO);
        System.out.println(result);
        return Result.ok(result);
    }

    //用户注册接口
    @PostMapping("/add")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO commandDTO){
        //调用服务层方法
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.register(commandDTO);
        return Result.ok(result);
    }

    //获取当前用户
    @GetMapping("/current")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser() {
        //从当前请求取 token
        String token = JwtTokenUtil.getCurrentToken();
        //解析 token（validateToken 内部已处理字符串→Long 的转换）
        JwtTokenUtil.TokenVerificationResult verification = JwtTokenUtil.validateToken(token);
        if (verification == null || !verification.isValid()) {
            throw new BusinessException("token无效");
        }
        //按 userId 查库返回
        return Result.ok(userService.getUserById(verification.getUserId()));
    }

}















