package com.atguigu.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    @RequestMapping("toTrade")
    public String toTrade(){


        return "redirect:http://passport.gmall.com:8085/login";

    }



    //数据库数据的更新
    @RequestMapping("checkCart")
    public String checkCart(CartInfo cartInfo ,HttpServletRequest  request,HttpServletResponse response,ModelMap map){
       //声明购物车集合
       List<CartInfo> cartInfos = new ArrayList<>();
        String skuId = cartInfo.getSkuId();
        String userId = "2";

        //修改购物车勾选状态，判断用户是否有登录
        if (StringUtils.isNotBlank(userId)){
            //登录修改db,修改db要同时用skuId和UserId
            cartInfo.setUserId(userId);
            //修改购物车数据
            cartService.updateCartByUserId(cartInfo);
            //从缓存中取出数据
           cartInfos =  cartService.getCartInfoFromCacheByUserId(userId);


        }else {

            //修改cookie , 页面获取Cookie字符串
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);

            //转化cookie  转化成购物车集合
            cartInfos = JSON.parseArray(listCartCookie, CartInfo.class);

            //更新cookie中的数据
            for (CartInfo info : cartInfos) {
                //判断要修改的商品id和当前的商品商品一样就得到新的选中状态
                String skuId1 = info.getSkuId();
                if (skuId1.equals(skuId)){
                    info.setIsChecked(cartInfo.getIsChecked());
                }
            }

            //覆盖浏览器
            CookieUtil.setCookie(request,response,"listCartCookie",JSON.toJSONString(cartInfos),1000*60*60*24,true);


        }

        //返回购物车列表的最新数据
        map.put("cartList",cartInfos);
        BigDecimal totalPrice = getTotalPrice(cartInfos);
        map.put("totalPrice",totalPrice);

        return "cartListInner";
    }




    @RequestMapping("cartList")
    public String cartList(ModelMap map,HttpServletRequest request){

       //取出购物车集合，根据UserId判断数据要从redis中取还是cookie取
        List<CartInfo> cartInfos =  new ArrayList<>();
       String userId = "2";
       if (StringUtils.isBlank(userId)){
           //用户id为空，从cookie中取数据
           String cookieValue = CookieUtil.getCookieValue(request, "listCartCookie", true);
           if (StringUtils.isNotBlank(cookieValue)){
               //转化成购物车集合字符串
                cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
           }
       }else {
           //用户登录从redis中取数据
           cartInfos = cartService.getCartInfoFromCacheByUserId(userId);
       }



        map.put("cartList",cartInfos);

        //被选中的商品的总价格
        BigDecimal totalPrice = getTotalPrice(cartInfos);
        map.put("totalPrice",totalPrice);

        return "cartList";

    }
    //购物车中被选中的商品的总价格
    private BigDecimal getTotalPrice(List<CartInfo> cartInfos) {

        BigDecimal totalPrice = new BigDecimal("0");
        //购物车中被选中的商品的总价格
        for (CartInfo cartInfo : cartInfos) {
            String isChecked = cartInfo.getIsChecked();
            if (isChecked.equals("1")){
                totalPrice = totalPrice.add(cartInfo.getSkuPrice());
            }
        }
      return totalPrice;
    }


    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request,HttpServletResponse response, @RequestParam Map<String,String> map ){

        //声明一个处理后的购物车集合
        List<CartInfo> cartInfos =  new ArrayList<>();

        //从购物车页面获取商品的详情Id
        String skuId = map.get("skuId");
        Integer skuNum = Integer.parseInt(map.get("num"));
        //根据商品Id得到商品的详情
        SkuInfo skuInfo = skuService.getSkuById(skuId);

        //创建一个购物车，把数据都放到购物车中.封装购物车对象
        CartInfo cartInfo = new CartInfo();
        //总价格=价格*数量
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(skuNum)));
        //数量
        cartInfo.setSkuNum(skuNum);
        cartInfo.setIsChecked("1");
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuName(skuInfo.getSkuName());
        //商品的单价
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());




        //判断用户是否登录
        String userId = "2";

        //添加购物车业务逻辑
        if (StringUtils.isBlank(userId)){

            cartInfo.setUserId("");
            //从页面拿到Cookie的值
            String cookieValue = CookieUtil.getCookieValue(request, "listCartCookie", true);

            //判断cookie中有没有购物车
            if (StringUtils.isBlank(cookieValue)){
                //如果cookie中没有数据，

             cartInfos.add(cartInfo);

            }else {
                //如果cookie有数据就转化成购物车字符串集合
                cartInfos = JSON.parseArray(cookieValue,CartInfo.class);

                //判断购物车有没有商品 有就更新没有就添加购物车
                boolean b = if_new_cart(cartInfos,cartInfo);
                if (b){
                    //新增购物车
                    cartInfos.add(cartInfo);

                }else {
                    //更新循环购物车的数据
                    for (CartInfo info : cartInfos) {
                        //如果购物车有数据，就判断，当前的要添加商品id和购物车里面的商品id一样，就把商品的价格和商品的数量相加
                        if (info.getSkuId().equals(cartInfo.getSkuId())){
                            info.setSkuNum(info.getSkuNum()+info.getSkuNum());//商品数量相加
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));//商品价格相乘商品的数量

                        }
                    }
                }
            }
            //将购物车放入cookie中
            CookieUtil.setCookie(request,response,"listCartCookie",JSON.toJSONString(cartInfos),1000*60*60*24,true);



        }else {
           //把用户Id放入购物车
            cartInfo.setUserId(userId);

            //根据用户Id和商品Id查询出购物车是否有商品（到数据库查询）
            //用户id和商品id必须同时存在，才查询，一个存在一个不存在就添加
       CartInfo  cartInfoDb = cartService.ifCartExits(cartInfo);
       //如果有购物车有数据就更新购车里面的商品
       if (null!=cartInfoDb){
           //更新购物车
           cartInfoDb.setSkuNum(cartInfoDb.getSkuNum() + cartInfo.getSkuNum());//商品的数量相加
           cartInfoDb.setCartPrice(cartInfoDb.getSkuPrice().multiply(new BigDecimal(cartInfoDb.getSkuNum())));//商品数量乘以价格
          cartService.updateCart(cartInfoDb);
       }else {
           //如果没有就添加购物车商品添加购物车
             cartService.insertCart(cartInfo);

          }

         //添加完购物车后缓存到redis中
          cartService.fiushCartCacheByUserId(userId);
        }




        return  "redirect:/cartSuccess";
    }


    //判断购物车有没有商品
    private boolean if_new_cart(List<CartInfo> listCartCookie, CartInfo cartInfo) {

        boolean b = true;
        for (CartInfo info : listCartCookie) {
            if (info.getSkuId().equals(cartInfo.getSkuId())){
                b= false;
            }
        }
        return b;
    }


    @RequestMapping("cartSuccess")
    public String cartSuccess(){

        return "success";
    }



}
