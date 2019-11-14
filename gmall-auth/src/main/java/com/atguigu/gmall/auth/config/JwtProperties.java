package com.atguigu.gmall.auth.config;

import com.atguigu.core.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
@Slf4j
@Data
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {
    /**
     *
     * auth:
     *   jwt:
     *     publicKeyPath: E:\\IDEA\myGmall\\tmp\\rsa.pub
     *     privateKeyPath: E:\\IDEA\myGmall\\tmp\\rsa.pri
     *     expire: 180
     *     cookieName: GMALL_TOKEN
     */
    //读取配置类

            //@PostConstruct
            //@PreDestroy
            //用来将转化为对象
    private  String publicKeyPath;

    private String privateKeyPath;

    private Integer  expire;

    private String cookieName;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    private String secret;

    @PostConstruct
    //PostConstruct 注释用于在依赖关系注入完成之后需要执行的方法上，
    // 以执行任何初始化。此方法必须在将类放入服务之前调用。
    public void init(){
        try {
        //1.初始化公私钥文件
        File privateFile = new File(publicKeyPath);
        File publicFile = new File(privateKeyPath);

        //2.检查文件队象是否为空
        if(!publicFile.exists() || !privateFile.exists()){

                RsaUtils.generateKey(publicKeyPath, privateKeyPath, secret);
            }

        //3.获取公钥或者私钥

             privateKey = RsaUtils.getPrivateKey(privateKeyPath);
             publicKey = RsaUtils.getPublicKey(publicKeyPath);
        }
        catch (Exception e) {

            log.error("初始化私钥或者公钥失败");

            e.printStackTrace();
        }


    }
}
