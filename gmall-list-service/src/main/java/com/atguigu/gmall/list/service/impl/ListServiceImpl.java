package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@Service
public class ListServiceImpl implements ListService{

    @Autowired
    JestClient jestClient;


    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam)  {

        ArrayList<SkuLsInfo> skuLsInfos = new ArrayList<>();
        Search search = new Search.Builder(getMyDsl(skuLsParam)).addIndex("gmall0508").addType("SkuLsInfo").build();

        //执行检索语句
        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //循环查询的数据，execute 这里没有循环的方法，因为已经封装好了再getHits()中
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = execute.getHits(SkuLsInfo.class);
        //循环hits的集合查询出数据
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo source = hit.source;


            //取出数据后关键字高亮
            Map<String, List<String>> highlight = hit.highlight;
            //highlight是elasticsearch 做高亮查询的，如果不为空就是有查询
            if (null!=highlight){

                List<String> skuName = highlight.get("skuName");
                if (StringUtils.isNotBlank(skuName.get(0))){
                    //如果关键字高亮不为空，则说明在检索时加入高亮

                    //循环取出来的数据换做高亮的数据
                    source.setSkuName(skuName.get(0));

                }

            }

            skuLsInfos.add(source);
        }


        return skuLsInfos;
    }



    //elasticsearch 查询的工具类(自动生成查询语句)
    public String getMyDsl(SkuLsParam skuLsParam){

        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String[] valueId = skuLsParam.getValueId();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //过滤
        if (StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        if (null!=valueId && valueId.length>0){
            //加载分类属性的条件
            for (int i = 0; i < valueId.length; i++) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueId[i]);
                boolQueryBuilder.filter(termQueryBuilder);

            }
        }


        //搜索
        if (StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }


        //将属性参数放入查询
        searchSourceBuilder.query(boolQueryBuilder);

        //设置分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);

        //设置关键字高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();

        highlightBuilder.preTags("<span style='color:red;font-weight:bolder'>");//字体加粗加红
        highlightBuilder.field("skuName");  //设置skuName查询高亮
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);//放入elasticsearch


        System.out.println(searchSourceBuilder.toString());
        //查询出的语句转成字符串形式
        return searchSourceBuilder.toString();


    }




}
