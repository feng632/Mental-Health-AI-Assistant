package com.example.aimentalhealth.service;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aimentalhealth.DTO.command.UserLoginCommandDTO;
import com.example.aimentalhealth.DTO.command.UserRegisterCommandDTO;
import com.example.aimentalhealth.DTO.response.UserLoginResponseDTO;
import com.example.aimentalhealth.common.Result;
import com.example.aimentalhealth.entity.User;
import com.example.aimentalhealth.enumClass.UserType;
import com.example.aimentalhealth.exception.BusinessException;
import com.example.aimentalhealth.mapper.UserMapper;
import com.example.aimentalhealth.service.convert.UserConvert;
import com.example.aimentalhealth.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import static javax.management.Query.or;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public UserLoginResponseDTO login(UserLoginCommandDTO commandDTO) {
        //构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, commandDTO.getUsername())
                .or()
                .eq(User::getEmail, commandDTO.getUsername());

        User user = userMapper.selectOne(queryWrapper);
        System.out.println(user);

        //判断用户是否存在
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        //判断密码是否正确
        String inputPassword = commandDTO.getPassword().trim();
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        //检查用户状态
        if (!user.isActive()) {
            throw new BusinessException("用户已被禁用,请联系管理员");
        }

        //生成token
        String token = JwtTokenUtil.generateToken(String.valueOf(user.getId()), user.getUsername(), user.getUserType());
        System.out.println(token);

        UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvert.convert(user);
        return UserConvert.entityToLoginResponse(token, userInfo);
    }


    //用户注册接口
    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO commandDTO) {
        System.out.println(JSONUtil.parseObj(commandDTO));
        //验证密码是否一致
        if (!commandDTO.getPassword().equals(commandDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入密码不一致");
        }

        //验证用户名是否存在
        LambdaQueryWrapper<User> userNameQueryWrapper = new LambdaQueryWrapper<>();
        userNameQueryWrapper.eq(User::getUsername, commandDTO.getUsername())
                .or()
                .eq(User::getEmail, commandDTO.getUsername());
        User user = userMapper.selectOne(userNameQueryWrapper);
        if (user != null) {
            throw new BusinessException("用户名或邮箱已存在");
        }

        //检查邮箱是否存在
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, commandDTO.getEmail());
        User emailUser = userMapper.selectOne(emailQuery);
        if (emailUser != null) {
            throw new BusinessException("邮箱已存在");
        }

        //用户类型
        if(!UserType.isValidCode(commandDTO.getUserType())){
            throw new BusinessException("用户类型无效");
        }

        //创建用户
        String password = commandDTO.getPassword().trim();
        String encodedPassword = passwordEncoder.encode(password);
        User userEntity = UserConvert.registerCommandToEntity(commandDTO, encodedPassword);
        userMapper.insert(userEntity);

        return UserConvert.convert(userEntity);
    }

    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        return UserConvert.convert(user);
    }
}