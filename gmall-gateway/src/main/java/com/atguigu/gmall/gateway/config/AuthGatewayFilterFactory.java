package com.atguigu.gmall.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties({JwtProperties.class})
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Autowired
    private AuthGatewayFilter authGatewayFilter;

    public GatewayFilter apply(Object config) {



        return authGatewayFilter;

    }
}
