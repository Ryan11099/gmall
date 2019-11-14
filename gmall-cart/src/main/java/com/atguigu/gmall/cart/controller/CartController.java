package com.atguigu.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.VO.Cart;
import com.atguigu.gmall.cart.VO.UserInfo;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 新增购物车
    @PostMapping
    public Resp<Object> addCart(@RequestBody Cart cart) {

        this.cartService.addCart(cart);

        return Resp.ok(null);
    }
    // 查询购物车
    @GetMapping
    public Resp<List<Cart>> queryCart(){
        List<Cart> carts = this.cartService.queryCarts();
        return Resp.ok(carts);

    }


    @PostMapping("update")
    public Resp<Object> updateCart(@RequestBody Cart cart) {

        this.cartService.updateCart(cart);

        return Resp.ok(null);
    }

    @PostMapping("{skuId}")
    public Resp<Object> deleteCart(@PathVariable("skuId")Long skuId){
        this.cartService.deleteCart(skuId);
        return Resp.ok(null);
    }

    @PostMapping("check")
    public Resp<Object> checkCart(@RequestBody List<Cart> carts){

        this.cartService.checkCart(carts);

        return Resp.ok(null);
    }

}