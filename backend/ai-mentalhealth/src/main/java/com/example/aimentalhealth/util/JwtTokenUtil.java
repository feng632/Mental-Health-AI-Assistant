package com.example.aimentalhealth.util;

import ch.qos.logback.core.util.StringUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.aimentalhealth.config.JwtConfig;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.Date;


@Component
public class JwtTokenUtil implements ApplicationContextAware {
    private static final String ISSUER = "AI-Mental-Health-Assistant";

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        JwtTokenUtil.applicationContext = applicationContext;
    }

    // 静态工具方法中想拿 Spring Bean 时使用
    public static JwtConfig getJwtConfig() {
        return applicationContext.getBean(JwtConfig.class);
    }

    // 生成token
    public static String generateToken(String userId, String username, Integer roleType) {
        try {
            // 生成token逻辑
            JwtConfig jwtConfig = getJwtConfig();
            //生成签名的算法
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
            //生成过期时间
            Date expiration = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());

            //生成token
            String token = JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("roleType", roleType)
                    .withExpiresAt(expiration)
                    .withIssuedAt(new Date())
                    .withIssuer(ISSUER)
                    .sign(algorithm);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("生成token失败" + e);

        }
    }

    public static String extractTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String tokenHeader = request.getHeader("token");
        if (StringUtils.hasText(tokenHeader)) {
            return tokenHeader;
        }
        return null;
    }

    // 验证token
    public static TokenVerificationResult validateToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        // userId 在 token 里是字符串，asLong() 对字符串会返回 null，需要手动转
        Long userId = null;
        String userIdStr = jwt.getClaim("userId").asString();
        if (StringUtils.hasText(userIdStr)) {
            userId = Long.valueOf(userIdStr);
        }
        String username = jwt.getClaim("username").asString();
        Integer roleType = null;

        try {
            roleType = jwt.getClaim("roleType").asInt();
        } catch (Exception e) {
            // 处理角色类型不存在的情况
            String roleTypeStr = jwt.getClaim("roleType").asString();
            if(StringUtils.hasText(roleTypeStr)) {
                roleType = Integer.valueOf(roleTypeStr);
            }
        }

        if(userId != null && StringUtils.hasText(username) && roleType != null) {
            return new TokenVerificationResult(userId, username, roleType, true);
        }




        return null;
    }

    //获取当前的token
    public static String getCurrentToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if(attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = (String) request.getAttribute("jwttoken");
            if(StringUtils.hasText(token)) {
                return token;
            }

            //备用方案：从请求头中获取token
            String headerToken = extractTokenFromRequest(request);
            return headerToken;
        }
        return null;
    }

    // 验证token有效性
    public static DecodedJWT verifyToken(String token) {
        if(!StringUtils.hasText(token)) {
            throw new JWTVerificationException("token不能为空");
        }

        //token解码
        JwtConfig jwtConfig = getJwtConfig();
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);

    }

//    public static

    //token验证结果封装类
    @Getter
    public static class TokenVerificationResult {
        private final Long userId;
        private final String username;
        private final Integer roleType;
        private final boolean valid;

        public TokenVerificationResult(Long userId, String username, Integer roleType, boolean valid) {
            this.userId = userId;
            this.username = username;
            this.roleType = roleType;
            this.valid = valid;
        }
    }
}