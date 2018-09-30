package com.atguigu.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;


import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

     //elasticsearch
	@Autowired
	JestClient jestClient;

	@Reference
	SkuService skuService;

	public static void main(String[] args) {

	}

	@Test
	public void contextLoads() throws IOException {

		ArrayList<SkuLsInfo> skuLsInfos = new ArrayList<>();

		Search search = new Search.Builder(getMyDsl()).addIndex("gmall0508").addType("SkuLsInfo").build();

		SearchResult execute = jestClient.execute(search);//异常往外抛

		//循环查询的数据，execute 这里没有循环的方法，因为已经封装好了再getHits()中
		List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
		//循环hits的集合查询出数据
		for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
			SkuLsInfo source = hit.source;
			skuLsInfos.add(source);
		}

		System.out.println(skuLsInfos.size());

	}


	//elasticsearch 查询的工具类(自动生成查询语句)
	public String getMyDsl(){
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		//bool查询
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

		//过滤
		TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id","61");
		boolQueryBuilder.filter(termQueryBuilder);

		//搜索
		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "小米");
		boolQueryBuilder.must(matchQueryBuilder);

		//将属性参数放入查询
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.from(0);
		searchSourceBuilder.size(100);
         //查询出的语句转成字符串形式
		return searchSourceBuilder.toString();


	}






	//查询出来的数据转化到elasticsearch中
	public void addData(){


		//查询skuInfo
		List<SkuInfo> skuInfoList = skuService.getSkuByCatalog3Id(61);

		//将skuInfo转化成skuLsInfo
		List<SkuLsInfo> skuLsInfos = new ArrayList<>();
		for (SkuInfo skuInfo : skuInfoList) {
			SkuLsInfo skuLsInfo = new SkuLsInfo();
			//阿帕奇的一个工具类把skuInfo中的bean中相同的字段转化成skuLsInfo
			BeanUtils.copyProperties(skuInfo,skuLsInfo);

			skuLsInfos.add(skuLsInfo);

		}

		//查看数据有没有转化
		System.out.println(skuLsInfos.size());
		//将SkuLsInfo插入到es中
		for (SkuLsInfo skuLsInfo : skuLsInfos) {
			Index build = new Index.Builder(skuLsInfo).index("gmall0508").type("SkuLsInfo").id(skuLsInfo.getId()).build();

			try {
				jestClient.execute(build);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}


}
