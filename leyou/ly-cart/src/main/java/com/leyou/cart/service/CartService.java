package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:uid:";
    public void addCart(Cart cart) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //判断当前购物车商品是否存在
        String key= KEY_PREFIX + user.getId();
        //hashKey
        String hashkey = cart.getSkuId().toString();
        //获取数量
        Integer num = cart.getNum();

        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        if (operation.hasKey(hashkey)){
            //是，修改数量
            String json = operation.get(hashkey).toString();
            cart = JsonUtils.parse(json, Cart.class);
            cart.setNum(cart.getNum() + num);
        }
        //否，新增
        operation.put(hashkey,JsonUtils.serialize(cart));

    }

    public List<Cart> queryCartList() {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key= KEY_PREFIX + user.getId();
        if (!redisTemplate.hasKey(key)){
            //key不存在，返回404
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        List<Cart> carts = operation.values().stream()
                .map(o -> JsonUtils.parse(o.toString(), Cart.class))
                .collect(Collectors.toList());

        return carts;

    }

    public void updateCartNum(Long skuId, Integer num) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key= KEY_PREFIX + user.getId();
        //hashkey
        String hashkey = skuId.toString();
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);

        if (!operation.hasKey(hashkey)){
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        //查询购物车
        Cart cart = JsonUtils.parse(operation.get(hashkey).toString(), Cart.class);
        cart.setNum(num);
        operation.put(hashkey,JsonUtils.serialize(cart));
    }

    public void daleteCart(Long skuId) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key= KEY_PREFIX + user.getId();
        redisTemplate.opsForHash().delete(key,skuId.toString());
    }
}
