package com.atguigu.gmall.list.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    ListService listService;


    @Reference
    AttrService attrService;

    //把要查询的数据封装在skuLsParam

    @RequestMapping("list.html")
    public String list(SkuLsParam skuLsParam, ModelMap map) {

        List<SkuLsInfo> skuLsInfoList = listService.search(skuLsParam);

        //取出属性值重复，用了HashSet集合(无序不可重复集合)
        //先遍历出属性值的集合，在集合里面遍历出全部属性值
        //把属性值放在HsahSet集合中，这样就不会出现重复
        HashSet<String> strings = new HashSet<>();
        for (SkuLsInfo skuLsInfo : skuLsInfoList) {
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            //遍历出平台属性值
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                strings.add(valueId);

            }
        }
        //把平台属性值（集合）转成字符串，并用，号隔开
        String join = StringUtils.join(strings, ",");
        List<BaseAttrInfo> baseAttrInfos = attrService.getAttrListByValueId(join);

        //制作当前面包屑
        List<Crumb> crumbs =   new ArrayList<>();


        //获得当前属性值的id
        String[] valueId = skuLsParam.getValueId();
        //有选择属性值的时候就遍历出被选中的属性值id并删除
        if (null!=valueId &&valueId.length>0){


            for (String sid : valueId) {
                //面包屑制作
                Crumb crumb = new Crumb();

                //删除当前请求中所包含的属性（把选中 出来的属性属性值放入使用迭代器）
                Iterator<BaseAttrInfo> iterator = baseAttrInfos.iterator();
                //使用hasNext()检查序列中是否还有元素
                while (iterator.hasNext()){
                    //使用next()获得序列中的下一个元素。
                    BaseAttrInfo baseAttrInfo = iterator.next();
                    //获得属性集合
                    List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();


                    //删除当前的valueId所关联的属性对象
                    for (BaseAttrValue baseAttrValue : attrValueList) {
                        String id = baseAttrValue.getId();

                        if (id.equals(sid)){
                            //设置面包屑名称
                            crumb.setValueName(baseAttrValue.getValueName());
                           //删除排除属性
                            iterator.remove();
                        }
                    }
                }
                 //制作面包屑
                String crumbsUrlParam =  getCrumbUrlParam(skuLsParam,sid);
                crumb.setUrlParam(crumbsUrlParam);
                crumbs.add(crumb);
            }
        }



        map.put("skuLsInfoList", skuLsInfoList);
        map.put("attrList", baseAttrInfos);

        //拼接当前请求地址
        String urlParam = getUrlParam(skuLsParam);
        map.put("urlParam", urlParam);

        map.put("attrValueSelectedList",crumbs);



        return "list";
    }


    /**
     * 面包屑的url
     */
    private String getCrumbUrlParam(SkuLsParam skuLsParam, String sid) {

        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueId = skuLsParam.getValueId();

        String urlParam = "";

        if (StringUtils.isNotBlank(catalog3Id)){

            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&" ;
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }


        if (StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&" ;

            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (null != valueId && valueId.length>0){
            for (String id : valueId) {
                if (!id.equals(sid)){
                    urlParam = urlParam + "&" + "valueId=" + id;
                }

            }
        }

     return urlParam;
    }



    /**
     *地址栏中的请求url
     * @param skuLsParam
     * @return
     */
    //拼接当前请求
    private String getUrlParam(SkuLsParam skuLsParam) {

        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueId = skuLsParam.getValueId();

        String urlParam = "";

        if (StringUtils.isNotBlank(catalog3Id)){

            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&" ;
            }
                 urlParam = urlParam + "catalog3Id=" + catalog3Id;
         }


        if (StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam + "&" ;

            }
             urlParam = urlParam + "keyword=" + keyword;
        }

        if (null != valueId && valueId.length>0){
            for (String id : valueId) {
                urlParam = urlParam + "&" + "valueId=" + id;
            }
        }

           return  urlParam;
    }

}
