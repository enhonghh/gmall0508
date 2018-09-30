package com.atguigu.com.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
public class userController {

    @Reference
    UserService userService;

    @RequestMapping("userlist")
    @ResponseBody
    public List<UserInfo> index(){

        List<UserInfo> userList = userService.getUserList();
        ;
    return  userList;
}

}
