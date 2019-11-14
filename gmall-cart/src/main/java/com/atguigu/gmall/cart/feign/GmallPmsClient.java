package com.atguigu.gmall.cart.feign;

import com.atguigu.pms.gmall.api.GmallPmsApi;
import com.atguigu.sms.gmall.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
