package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.SkuImageMapper;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class SkuServiceImpl  implements SkuService {
    //要查询，保存，修改，删除，就@Autowired那张表

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public List<SkuInfo> skuInfoListBySpu(String spuId) {


        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        List<SkuInfo> select = skuInfoMapper.select(skuInfo);
        return select;
    }


    @Override
    public void saveSku(SkuInfo skuInfo) {

        // 保存sku信息
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();

        // 保存平台属性关联信息
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);
        }

        // 保存销售属性关联信息
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }


        // 保存sku图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insert(skuImage);
        }

    }

    /**
     * 从db查询sku详情
     * @return
     */

    public SkuInfo getSkuByIdFromDb(String skuId){

        //查询sku信息,按skuId查询
        SkuInfo skuInfoParam = new SkuInfo();
        //查询出id，并且只有一个值
        skuInfoParam.setId(skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(skuInfoParam);

        //查询图片集合
        SkuImage skuImageParam = new SkuImage();
        skuImageParam.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImageParam);
        skuInfo.setSkuImageList(skuImages);

        return  skuInfo;
    }



    /**
       redis缓存查询sku详情
     */
    @Override
    public SkuInfo getSkuById(String skuId) {

        SkuInfo skuInfo = null;
        String skuKey = "shu:" + skuId  +":info" ;
        //缓存redis查询
        Jedis jedis = redisUtil.getJedis();
        //skuKey是redis缓存的key值
        String s = jedis.get(skuKey);


        //2如果第一个访问本数据为空的时候，第二个后面的用户就不需要再进入访问，直接放回null2
        if (StringUtils.isNotBlank(s)&&s.equals("kong")){
            return  null;
        }



             //如果缓存中不为空，就是有值，直接调用缓存的数据，否则就调用数据库的数据
        if (StringUtils.isNotBlank(s)){
             skuInfo = JSON.parseObject(s, SkuInfo.class);
        }else {

            //设置redis分布式锁，防止redis并发超标死机，在第一个redis死机前，先访问备用的redis
            //以免直接访问数据库造成项目坍塌
            //正常项目要在额外创建一个新配置一个redis，本项目暂时先不配置，还是用原来的redis做案例
            //nx是唯一锁，px是设置锁的时间
            String OK = jedis.set("shu" + skuId + "lock", "1", "nx", "px", 10000);
                 //如果所不为空就访问数据库
                 if (StringUtils.isNotBlank(OK)){
                     //查询数据库(设置好时间这段时间可以访问)
                     skuInfo =  getSkuByIdFromDb(skuId);

                 }else{
                     try {
                         Thread.sleep(3000);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }

                     jedis.close();
                     //自旋return 就是自旋完直接结束线程，没有return就是调用一个新的方法，会走一个新的子线程，程序自旋的时候会从新调用，
                     return   getSkuById(skuId);
                 }

                 //判断，如果访问数据库的值都为空的话，就返回一个值，保存在redis中，告诉后面访问的人
                 //数据为空
                  if(null == skuInfo){
                     //保存一个空值在redis中，设置在门口，其他人访问本数据的时候直接返回null，不在进去2
                     jedis.set(skuKey,"kong");
                  }


          if (null!=skuInfo){
              //同步到redis
              jedis.set(skuKey,JSON.toJSONString(skuInfo));
          }
          jedis.close();


        }

        return skuInfo;
    }






    //查询销售属性选中
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String spuId ,String skuId) {

        List<SpuSaleAttr> spuSaleAttrs =   skuSaleAttrValueMapper.selectSpuSaleAttrListCheckBySku(Integer.parseInt(spuId),Integer.parseInt(skuId));
        return spuSaleAttrs;
    }
    //查询sku销售属性的对应关系
    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {

        List<SkuInfo> skuInfos = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(Integer.parseInt(spuId));
        return skuInfos;
    }

    @Override
    public List<SkuInfo> getSkuByCatalog3Id(int catalog3Id) {

        //查询sku信息,按skuId查询
        SkuInfo skuInfoParam = new SkuInfo();
        //查询出三级分类Id
        skuInfoParam.setCatalog3Id(catalog3Id + "");
       List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfoParam);

        for (SkuInfo skuInfo : skuInfos) {

            String skuId = skuInfo.getId();
             //查询图片集合
            SkuImage skuImageParam = new SkuImage();
            skuImageParam.setSkuId(skuId);
            List<SkuImage> skuImages = skuImageMapper.select(skuImageParam);
            skuInfo.setSkuImageList(skuImages);

            //平台属性集合
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(skuId);
            List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);

            skuInfo.setSkuAttrValueList(skuAttrValues);

        }


        return skuInfos;

    }



}