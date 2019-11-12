package com.atguigu.gmall.auth.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    //判断用户是否登录
    @PostMapping("accredit")
    public Resp<Object> accredit(@RequestParam("username")String userName , @RequestParam("password")String password){

        this.authService.accredit(userName,password);

        return Resp.ok(null);
    }
}
