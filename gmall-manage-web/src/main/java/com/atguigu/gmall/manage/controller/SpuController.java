package com.atguigu.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.manage.util.FileUploadUtil;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class SpuController {

    @Reference
    SpuService spuService;


    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){

        List<SpuImage> spuImages =  spuService.spuImageList(spuId);

        return  spuImages;
    }


    //获取销售属性//按商品的属性ID查询数据
    @RequestMapping("spuSaleAttrsList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrsList(String spuId){

        List<SpuSaleAttr> spuSaleAttrs =  spuService.spuSaleAttrsList(spuId);

        return  spuSaleAttrs;
    }


    //图片上传
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file){

        // 调用上传工具
       String imgUrl =  FileUploadUtil.uploadImage(file);

        return imgUrl;
    }




    //刷新列表获取属性的详细信息（根据三级id查询出全部数据）
    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> spuList(String catalog3Id){

        List<SpuInfo> spuInfos = spuService.spuList(catalog3Id);

        return spuInfos;
    }



    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){

    List<BaseSaleAttr> baseSaleAttrs = spuService.baseSaleAttrList();

        return baseSaleAttrs;
    }



    //商品销售属性添加
    @RequestMapping("saveSpu")
    @ResponseBody
    public String saveSpu(SpuInfo spuInfo){

     spuService.saveSpu(spuInfo);

        return "保存成功";

    }

}
