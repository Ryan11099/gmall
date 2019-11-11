package com.atguigu.sms.gmall.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.sms.gmall.vo.ItemSaleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.atguigu.sms.gmall.vo.SaleVO;

import java.util.List;

public interface GmallSmsApi {
    //对外提供远程调用的接口

    @GetMapping("sms/skubounds/item/sales/{skuId}")
    public Resp<List<ItemSaleVO>> queryItemSaleVOs(@PathVariable("skuId")Long skuId);

    @PostMapping("sms/skubounds/sale")
    public Resp<Object> saveSale(@RequestBody SaleVO saleVO);
}
