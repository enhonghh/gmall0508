package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class AttrController {

    @Reference
    AttrService attrService;

    @RequestMapping("saveAttr")
    @ResponseBody
    public String saveAttr(BaseAttrInfo baseAttrInfo){
        //baseAttrInfo是属性的名字，BaseAttrValue是属性的值，值是包含尺寸啊，内存啊等

      attrService.saveAttr(baseAttrInfo);

        return "保存成功";
    }


    @RequestMapping("getAttrListByCtg3")
    @ResponseBody
    public List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id){
        //三级对象的数据
        List<BaseAttrInfo> baseAttrInfos = attrService.getAttrListByCtg3(catalog3Id);

        return baseAttrInfos;
    }
}
