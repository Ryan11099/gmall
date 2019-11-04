package com.atguigu.sms.gmall.api;

import com.atguigu.core.bean.Resp;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.atguigu.sms.gmall.vo.SaleVO;

public interface GmallSmsApi {
    //对外提供远程调用的接口
    @PostMapping("sms/skubounds/sale")
    public Resp<Object> saveSale(@RequestBody SaleVO saleVO);
}
