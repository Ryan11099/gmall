package com.atguigu.gmall.pms.feign;

import com.atguigu.sms.gmall.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
    //这里的远程调用实现了生产方与消费方的分离，
}
