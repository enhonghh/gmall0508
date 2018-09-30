package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.manage.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.HashSet;
import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> getAttrListByCtg3(String catalog3Id) {

        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
         //获取三级分类的id
        baseAttrInfo.setCatalog3Id(catalog3Id);
        //返回数据
        List<BaseAttrInfo> select = baseAttrInfoMapper.select(baseAttrInfo);

        if (null != select && select.size()>0){

            for (BaseAttrInfo attrInfo : select) {
                String attrId = attrInfo.getId();

                BaseAttrValue baseAttrValue = new BaseAttrValue();
                baseAttrValue.setAttrId(attrId);
                List<BaseAttrValue> select1 = baseAttrValueMapper.select(baseAttrValue);
                attrInfo.setAttrValueList(select1);

            }
        }

        return select;
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {

        String id = baseAttrInfo.getId();
        //判断如果没有主键id就保存，如果有主键id就更新
        if (StringUtils.isNotBlank(id)){
            //insert把数据存入（空值也存入）
            //insertSelective把数据存入（空值不存入），并自动生成主键
            baseAttrInfoMapper.insertSelective(baseAttrInfo);

            //获取主键
            String attrId = baseAttrInfo.getId();

            //获取属性值的集合
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            //baseAttrInfo是属性的名字，BaseAttrValue是属性的值，值是包含尺寸啊，内存啊等
            //遍历出属性的值
            for (BaseAttrValue baseAttrValue : attrValueList) {
                //获取属性的id值
                baseAttrValue.setAttrId(attrId);
                //按属性id保存数据
                baseAttrValueMapper.insert(baseAttrValue);

            }
        }else {
               //扩增，也可以不需要一下代码，不需要下面的代码也可以，直接保存数据即可，这里只是做了一个扩增
              //保存的同时 更新数据
             baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);

            Example example = new Example(BaseAttrValue.class);
            //BaseAttrValue的id等于baseAttrInfo的Id时，删除全部数据，从新添加数据
            example.createCriteria().andNotEqualTo("attrId",baseAttrInfo.getId());
            baseAttrValueMapper.deleteByExample(example);

           //获取主键
            String attrId = baseAttrInfo.getId();
            //保存属性值
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();


            //循环保存(属性值可能传入多个)
            for (BaseAttrValue baseAttrValue : attrValueList) {
                //按主键保存
                baseAttrValue.setAttrId(attrId);
                baseAttrValueMapper.insert(baseAttrValue);


            }
        }

    }



    @Override
    public List<BaseAttrInfo> getAttrListByValueId(String join) {

        //根据页面传进来的平台属性值id（集合）查询出数据，
        List<BaseAttrInfo> baseAttrInfos =  baseAttrInfoMapper.selectAttrListByValueId(join);

        return baseAttrInfos;

    }

}
