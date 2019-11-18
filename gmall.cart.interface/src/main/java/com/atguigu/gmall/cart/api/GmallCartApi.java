package com.atguigu.gmall.cart.api;

import com.atguigu.core.bean.Resp;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.atguigu.gmall.cart.vo.CartItemVO;

import java.util.List;

public interface GmallCartApi {



    @GetMapping("cart/order/{userId}")
    public Resp<List<CartItemVO>> queryCartItemVO(@PathVariable("userId") Long userId);
}
