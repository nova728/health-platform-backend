package com.health.healthplatform.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.health.healthplatform.util.JWTUtils; // 确保JWTUtils类存在
import com.health.healthplatform.util.BaseUserInfo; // 导入BaseUserInfo
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyHandler implements HandlerInterceptor {

    //@Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
//        //获得请求头里的TOKEN数据
//        String token = request.getHeader("token");
//        //根据token解析数据，因为配置了全局异常处理中心，如果有异常会在全局异常中心处理异常
//        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
//        //获得用户信息
//        String name = tokenInfo.getClaim("name").asString();
//        String pass = tokenInfo.getClaim("pass").asString();
//        //存储用户信息到ThreadLocal中
//        BaseUserInfo.set("name",name);
//        BaseUserInfo.set("pass",pass);
//        return true;
//    }
}

