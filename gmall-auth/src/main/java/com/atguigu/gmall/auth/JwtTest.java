package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
	private static final String pubKeyPath = "E:\\IDEA\\myGmall\\tmp\\rsa.pub";

    private static final String priKeyPath = "E:\\IDEA\\myGmall\\tmp\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }//生成公钥和私钥

    //secret为加的盐，越复杂越好
    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzM1NTY3ODZ9.AZEDyygj4Yv6HSwMIWiIMi0w8RUnmL3njRI14VXEao8kVXffLAp5tJt19YKZQO_C2UJ2tlc_rLS14v0vEJWZyfgf7QNQFYf1xIiLIGSmIA6S-YQZNARHD9Ve3p8Ca_8NBGNLzlBhGgzrcWXilYScwWuj2piAXB3iDA8GmV7FU_I-m1qS5F45yTZfAGQyT8y9DBcymOsFfo0R-d1xta8StrzKCP6v0k8EvzCburvuyR0OIVLSpu3Sde7K3eLLNDifEBL-JyKL43eWgtS7RMQgqnhu_7SFV8v_30mRpH0iB4Hk6qpBaWNOdZJvLyZn5_d5PUMH7l1itn3xptjGTwonsQ";


        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}