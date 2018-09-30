package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;


    //根据用户Id和商品Id查询出购物车，看看是否购物车有数据
    @Override
    public CartInfo ifCartExits(CartInfo cartInfo) {

        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId", cartInfo.getUserId())
                .andEqualTo("skuId", cartInfo.getSkuId());

        CartInfo cartInfoSelect = cartInfoMapper.selectOneByExample(example);

        return cartInfoSelect;
    }

    //更新购物车数据
    @Override
    public void updateCart(CartInfo cartInfoDb) {
        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDb);

        //同步缓存到redis中
        fiushCartCacheByUserId(cartInfoDb.getUserId());

    }

    //添加购物车
    @Override
    public void insertCart(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);

    }


    //把购物车数据缓存到redis中
    @Override
    public void fiushCartCacheByUserId(String userId) {


        //根据UserId查询购物车集合
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);


        if (null != cartInfos && cartInfos.size() > 0) {
            //将购物车集合转化为map
            Map<String, String> map = new HashMap<String, String>();
            for (CartInfo info : cartInfos) {
                map.put(info.getId(), JSON.toJSONString(info));
            }

            Jedis jedis = redisUtil.getJedis();
            //将购物车集合以key，value 缓存到redis中
            jedis.hmset("cart:" + userId + ":list", map);

            jedis.close();
        } else {
            //清理redis
            Jedis jedis = redisUtil.getJedis();
            //将购物车集合以key，value 缓存到redis中
            jedis.hmset("cart:" + userId + ":list", null);

            jedis.close();
        }
    }

    //从缓存中取数据
    @Override
    public List<CartInfo> getCartInfoFromCacheByUserId(String userId) {
        //声明一个购物车集合
        ArrayList<CartInfo> cartInfos = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("cart:" + userId + ":list");
        //根据缓存中的key取出数据，遍历出数据，放入到购物车集合中
        if (null!=hvals &&hvals.size()>0){
            for (String hval : hvals) {
                CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);
                cartInfos.add(cartInfo);
            }
        }
        jedis.close();


        return cartInfos;
    }


    //修改购物车选中价格db,要同时根据skuId和userId
    @Override
    public void updateCartByUserId(CartInfo cartInfo) {

        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("skuId", cartInfo.getSkuId()).andEqualTo("userId", cartInfo.getUserId());

        cartInfoMapper.updateByExampleSelective(cartInfo,example);

        //同步缓存到redis中
        fiushCartCacheByUserId(cartInfo.getUserId());

    }

}