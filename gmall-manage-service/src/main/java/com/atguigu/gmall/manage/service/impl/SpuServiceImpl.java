package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService{

    @Autowired
    BaseSeleAttrMapper baseSeleAttrMapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SpuImageMapper spuImageMapper;


    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrs = baseSeleAttrMapper.selectAll();
        return baseSaleAttrs;
    }


    //保存数据
    @Override
    public void saveSpu(SpuInfo spuInfo) {

        //保存spu信息， 生成spu的主键
        // 前台传进来的数据在数据库的字段有的为空，如果是非空就不插入，
        spuInfoMapper.insertSelective(spuInfo);

        // 在插入的同时把数据库的主键封装给 spu的主键Id   spuInfoId
        String spuInfoId = spuInfo.getId();

        //保存销售属性的信息,（保存销售属性带spu-id和spu-Attr-id存储）

        //获取销售属性的集合
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //遍历销售属性，并带保存spuInfoId
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {

            //销售属性带spu-id，一起保存到数据库
            spuSaleAttr.setSpuId(spuInfoId);
            //保存销售属性
            spuSaleAttrMapper.insertSelective(spuSaleAttr);

            //在保存销售属性的同时保存销售属性值信息，获取销售属性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();

            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                //带spuid保存
                spuSaleAttrValue.setSpuId(spuInfoId);
                //保存销售属性值
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);

            }

        }

       //保存图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();

        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfoId);
            spuImageMapper.insertSelective(spuImage);

        }


    }

    @Override
    public List<SpuInfo> spuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();

        //根据三级分类的id查询
        spuInfo.setCatalog3Id(catalog3Id);

        List<SpuInfo> select = spuInfoMapper.select(spuInfo);


        return select;
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrsList(String spuId) {
        //按商品属性id查询出商品销售
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);
        //查询出销售属性
        List<SpuSaleAttr> select = spuSaleAttrMapper.select(spuSaleAttr);

        if (null !=select && select.size()>0) {
            //遍历出商品销售属性值
            for (SpuSaleAttr saleAttr : select) {

                SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
                //按商品属性和商品销售属性 查询出商品销售属性值
                spuSaleAttrValue.setSpuId(spuId);
                spuSaleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());
                List<SpuSaleAttrValue> select1 = spuSaleAttrValueMapper.select(spuSaleAttrValue);

                //吧销售属性值保存到销售属性中
                saleAttr.setSpuSaleAttrValueList(select1);

            }
        }
        //返回销售属性(包含了销售属性和销售属性值)
        return select;
    }

    @Override
    public List<SpuImage> spuImageList(String spuId) {

        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> select = spuImageMapper.select(spuImage);
        return select;
    }
}
