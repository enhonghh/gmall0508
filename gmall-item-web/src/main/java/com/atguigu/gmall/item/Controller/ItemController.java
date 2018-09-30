package com.atguigu.gmall.item.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuService;
import org.assertj.core.util.Strings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @RequestMapping("dome")
    public String dome(ModelMap map){

        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i <=5 ; i++) {
            strings.add("thymeleaf" + i);
        }
        map.put("hello","hello thymeleaf !");
        map.put("list" ,strings);
        map.put("ifFlag","1");

        return "dome";
    }



    @RequestMapping("{skuId}.html")
    public String index(@PathVariable String skuId,ModelMap map){
       //当前sku
       SkuInfo skuInfo = skuService.getSkuById(skuId);
        map.put("skuInfo",skuInfo);

        String spuId = skuInfo.getSpuId();

        //查询销售属性列表,查询出来后对应的选中
      List<SpuSaleAttr> spuSaleAttrs =  skuService.getSpuSaleAttrListCheckBySku(spuId,skuInfo.getId());
      map.put("spuSaleAttrListCheckBySku",spuSaleAttrs);

        //查询sku的兄弟姐妹的hash表
        HashMap<String, String> skuMap = new HashMap<String,String>();

        List<SkuInfo> skuInfos = skuService.getSkuSaleAttrValueListBySpu(spuId);

        for (SkuInfo info : skuInfos) {
            //得到SkuInfo的id，
            String v = info.getId();
            String k = "";
            //得到SkuSaleAttrValue的对象
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
             //遍历SkuSaleAttrValue的值
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                //sku属性值的Id
                String saleAttrValueId = skuSaleAttrValue.getSaleAttrValueId();

                k =k + "|" +  saleAttrValueId;
            }

            skuMap.put(k,v);
        }

        //用json工具将hashmap转化成json字符串
        String skuMapJson = JSON.toJSONString(skuMap);

        map.put("skuMapJson",skuMapJson);
        System.out.println(skuMapJson);


        return "item";
    }

}
