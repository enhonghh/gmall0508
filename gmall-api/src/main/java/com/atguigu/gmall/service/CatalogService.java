package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.bean.BaseCatalog3;

import java.util.List;


public interface CatalogService {
     //查询一级表数据
    List<BaseCatalog1> getCatalog1();
    //查询二级表数据
    List<BaseCatalog2> getCatalog2(String catalog1Id );
    //查询三级表数据
    List<BaseCatalog3> getCatalog3(String catalog2Id);


}
