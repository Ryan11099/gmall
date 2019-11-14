package com.atguigu.gmall.auth.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("auth")

public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    //判断用户是否登录
    @PostMapping("accredit")
    public Resp<Object> accredit(@RequestParam("username")String userName , @RequestParam("password")String password , HttpServletRequest request , HttpServletResponse response){

        String jwtToken = this.authService.accredit(userName, password);


        if (StringUtils.isEmpty(jwtToken)){
            return Resp.fail("xxx");
        }
        // 4.将得到的值放入cookie中去
        CookieUtils.setCookie(request, response,this.jwtProperties.getCookieName(),jwtToken , this.jwtProperties.getExpire()*60);
        //cookie 的默认时间单位是秒，我们这里设置的是分钟
        return Resp.ok(null);
    }
}
