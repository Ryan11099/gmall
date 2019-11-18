package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.VO.Cart;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.vo.CartItemVO;
import com.atguigu.pms.gmall.entity.SkuInfoEntity;
import com.atguigu.pms.gmall.entity.SkuSaleAttrValueEntity;
import com.atguigu.sms.gmall.vo.ItemSaleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    public static final String CURRENT_PRICE_PREFIX = "cart:price:";

    private static final String KEY_PREFIX = "cart:key:";



    public void addCart(Cart cart) {
        // 这里的key是前方用于判断用户是否登录的状态码，如果已经登录，则会拥有自己的userID。否则就是usserkey
        String key = getKey();
        // 判断购物车中是否有该记录，传过来的key值已经确定了是否进行过登录
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        // 取出用户新增购物车商品的数量
        Integer count = cart.getCount();
        String skuId = cart.getSkuId().toString();
        // 有更新数量
        if (hashOps.hasKey(cart.getSkuId().toString())){
            // 不论是skuID还是cart都是String类型的
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);// 将cartJson进行反序列化成为Cart对象
            // 更新数量
            cart.setCount(cart.getCount() + count);
        } else {
            // 没有新增记录
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            cart.setTitle(skuInfoEntity.getSkuTitle());
            cart.setCheck(true);
            cart.setPrice(skuInfoEntity.getPrice());
            // 查询销售属性
            Resp<List<SkuSaleAttrValueEntity>> listResp;
            listResp = this.gmallPmsClient.querySaleAttrBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = listResp.getData();
            cart.setSkuAttrValue(skuSaleAttrValueEntities);
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            // 查询营销信息
            Resp<List<ItemSaleVO>> listResp1 = this.gmallSmsClient.queryItemSaleVOs(cart.getSkuId());
            cart.setSales(listResp1.getData());
            // 将最新的价格设置到redis中去
            this.redisTemplate.opsForValue().set(CURRENT_PRICE_PREFIX+skuId, skuInfoEntity.getPrice().toString());

        }
        // 同步到redis中
        hashOps.put(skuId, JSON.toJSONString(cart));
    }



    //用于获取登录状态
    private String getKey() {
        String key = KEY_PREFIX;
        // 判断登录状态
        UserInfo userInfo = LoginInterceptor.get();
        if (userInfo.getUserId() != null) {
            // 不为空说明已经登录过了，
            key += userInfo.getUserId();
        } else {
            key += userInfo.getUserKey();
        }
        return key;
    }




    // 查看购物车
    public List<Cart> queryCarts() {

        // 查询未登录状态的购物车
        UserInfo userInfo = LoginInterceptor.get();
        String key1 = KEY_PREFIX + userInfo.getUserKey();

        BoundHashOperations<String, Object, Object> userKeyOps = this.redisTemplate.boundHashOps(key1);
        List<Object> cartJsonList = userKeyOps.values();
        List<Cart> userKeyCarts = null;
        if (!CollectionUtils.isEmpty(cartJsonList)) {
            userKeyCarts = cartJsonList.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PREFIX + cart.getSkuId())));
                return cart;
            }).collect(Collectors.toList());
        }

        // 判断登录状态
        if (userInfo.getUserId() == null) {
            // 未登录直接返回
            return userKeyCarts;
        }

        // 登录，查询登录状态的购物车
        String key2 = KEY_PREFIX + userInfo.getUserId();
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key2);
        // 判断未登录的购物车是否为空
        if (!CollectionUtils.isEmpty(userKeyCarts)) {
            // 不为空，合并
            userKeyCarts.forEach(cart -> {
                // 有更新数量
                if (userIdOps.hasKey(cart.getSkuId().toString())){
                    String cartJson = userIdOps.get(cart.getSkuId().toString()).toString();
                    Cart idCart = JSON.parseObject(cartJson, Cart.class);
                    // 更新数量
                    idCart.setCount(idCart.getCount() + cart.getCount());
                    userIdOps.put(cart.getSkuId().toString(), JSON.toJSONString(idCart));
                } else {
                    // 没有新增记录
                    userIdOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
                }
            });
            this.redisTemplate.delete(key1);
        }
        // 查询返回
        List<Object> userIdCartJsonList = userIdOps.values();
        if (CollectionUtils.isEmpty(userIdCartJsonList)){
            return null;
        }
        return userIdCartJsonList.stream().map(userIdCartJson -> {
            Cart cart = JSON.parseObject(userIdCartJson.toString(), Cart.class);
            cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PREFIX + cart.getSkuId())));
            return cart;
        }).collect(Collectors.toList());
    }

    // 更新购物车记录
    public void updateCart(Cart cart) {

        String key = getKey();

        Integer count = cart.getCount();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            // 获取购物车中的更新数量的购物记录
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }
    }

    // 删除购物车记录
    public void deleteCart(Long skuId) {

        String key = getKey();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {

            hashOps.delete(skuId.toString());
        }
    }


    // 选中状态的勾选
    public void checkCart(List<Cart> carts) {

        String key = getKey();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        carts.forEach(cart -> {
            Boolean check = cart.getCheck();
            if (hashOps.hasKey(cart.getSkuId().toString())) {
                // 获取购物车中的更新数量的购物记录
                String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
                cart = JSON.parseObject(cartJson, Cart.class);
                cart.setCheck(check);
                hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            }
        });
    }

    public List<CartItemVO> queryCartItemVO(Long userId) {

        // 登录，查询登录状态的购物车
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key);
        // 查询返回
        List<Object> userIdCartJsonList = userIdOps.values();
        if (CollectionUtils.isEmpty(userIdCartJsonList)){
            return null;
        }
        // 获取所有的购物车记录
        return userIdCartJsonList.stream().map(userIdCartJson -> {
            Cart cart = JSON.parseObject(userIdCartJson.toString(), Cart.class);
            cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(CURRENT_PRICE_PREFIX + cart.getSkuId())));
            return cart;
        }).filter(cart -> cart.getCheck()).map(cart -> {// 获取已经选中的的购物车记录
            CartItemVO cartItemVO = new CartItemVO();// 将cart对象转化为CartVO对象中去
            cartItemVO.setSkuId(cart.getSkuId());
            cartItemVO.setCount(cart.getCount());
            return cartItemVO;
        }).collect(Collectors.toList());

    }
}
