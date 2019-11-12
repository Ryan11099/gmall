package com.atguigu.gmall.auth.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public void accredit(String userName, String password) {

        //远程调用用户中心的数据接口，进行查询


        //判断用户是否存在，不存在直接返回

        //如果用户存在的话，生成jwt


        //把生成的jwt放入到cookie中去

    }
}
