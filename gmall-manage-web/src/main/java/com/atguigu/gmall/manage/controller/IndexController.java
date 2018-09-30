package com.atguigu.gmall.manage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {
    //属性管理
    @RequestMapping("attrListPage")
    public String attrListPage(){

        return "attrListPage";
    }

    //商品管理spu
    @RequestMapping("spuListPage")
    public String spuListPage(){

        return "spuListPage";
    }



    @RequestMapping("index")
    public String index(){

        return "index";
    }

}
