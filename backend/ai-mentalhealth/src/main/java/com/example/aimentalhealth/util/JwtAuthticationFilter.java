package com.example.aimentalhealth.util;

import cn.hutool.extra.validation.BeanValidationResult;
import cn.hutool.json.JSONUtil;
import com.example.aimentalhealth.DTO.response.UserLoginResponseDTO;
import com.example.aimentalhealth.common.ResultCode;
import com.example.aimentalhealth.config.securityConfig;
import com.example.aimentalhealth.enumClass.UserStatus;
import com.example.aimentalhealth.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JwtAuthticationFilter extends OncePerRequestFilter {
    @Resource
    private UserService userService;
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return securityConfig.isPublicPath(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        //获取请求Url和方法
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        System.out.println(requestURI);
        System.out.println(method);

        //从请求头中获取token
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        if(StringUtils.hasText(token)) {
            //验证token并获取用户信息
            JwtTokenUtil.TokenVerificationResult validationResult = JwtTokenUtil.validateToken(token);
            if(validationResult != null && validationResult.isValid()) {
                //查询用户信息验证用户状态
                UserLoginResponseDTO.UserDetailResponseDTO user = userService.getUserById(validationResult.getUserId());
                System.out.println(JSONUtil.parseObj(user));
                if(user != null && UserStatus.NORMAL.getCode().equals(user.getStatus())) {
                    //创建Spring Security认证对象
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_"+ validationResult.getRoleType())
                            );

                    //创建UsernamePasswordAuthenticationToken对象
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            validationResult.getUsername(),
                            null,
                            authorities
                            );
                    //设置Spring Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    //将token存储到请求的属性中
                    request.setAttribute("jwttoken", token);

                }else{
                    //清理上下文
                    clearSecurityContext();
                    ResponseUtil.writeError(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
                    return;
                }



            }else{
                //清理上下文
                clearSecurityContext();
                ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
                return;
            }

        }else{
            //清理上下文
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;
        }

        //继续执行链
        chain.doFilter(request, response);
    }


    //清理Spring Security上下文
    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}

