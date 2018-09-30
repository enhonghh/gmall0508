package com.atguigu.com.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
public class AddressController {

    @Reference
    UserService userService;

     @RequestMapping("index2")
     @ResponseBody
     public List<UserAddress>  index2(){

         List<UserAddress> userAddress = userService.getUserAddress();
         return userAddress;

     }


}
