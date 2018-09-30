package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    CartInfo ifCartExits(CartInfo cartInfo);

    void updateCart(CartInfo cartInfoDb);

    void insertCart(CartInfo cartInfo);

    void fiushCartCacheByUserId(String userId);

    List<CartInfo> getCartInfoFromCacheByUserId(String userId);

    void updateCartByUserId(CartInfo cartInfo);
}
