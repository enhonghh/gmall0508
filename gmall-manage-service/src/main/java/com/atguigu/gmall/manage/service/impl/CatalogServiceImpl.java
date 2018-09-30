package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.bean.BaseCatalog3;
import com.atguigu.gmall.manage.mapper.BeanCatalog1Mapper;
import com.atguigu.gmall.manage.mapper.BeanCatalog2Mapper;
import com.atguigu.gmall.manage.mapper.BeanCatalog3Mapper;
import com.atguigu.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class CatalogServiceImpl implements CatalogService{

    @Autowired
    BeanCatalog1Mapper beanCatalog1Mapper;

    @Autowired
    BeanCatalog2Mapper beanCatalog2Mapper;

    @Autowired
    BeanCatalog3Mapper beanCatalog3Mapper;


    @Override
    public List<BaseCatalog1> getCatalog1() {

        List<BaseCatalog1> baseCatalog1s = beanCatalog1Mapper.selectAll();
        return baseCatalog1s;
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        //二级列表要传入一级列的id，先创建好二级列表，
        // 然后二级列表中的set方法中传入一级列表的id
        List<BaseCatalog2> select = beanCatalog2Mapper.select(baseCatalog2);

        return select;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
       //三级列表呀传入二级列表的id。先创建好三级列表的对象
        //然后三级列表获取2级列表的set方法
        List<BaseCatalog3> select = beanCatalog3Mapper.select(baseCatalog3);
        return select;
    }
}
