package com.atguigu.gmall.search.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.ChildrenAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private JestClient jestClient;

    public SearchResponse search(SearchParamVO searchParamVO) {
        try {
            //根据用户传过来的语句进行构建dsl语句
            String dsl = buildDSL(searchParamVO);
            Search search = new Search.Builder(dsl).addIndex("goods").addType("info").build();
            SearchResult searchResult = this.jestClient.execute(search);

            SearchResponse response = parseResult(searchResult);
//            SearchResult response = this.jestClient.execute(search);
            // 分页参数
            //从用户的传入数据中即可获得，所以直接设置
            response.setPageSize(searchParamVO.getPageSize());
            response.setPageNum(searchParamVO.getPageNum());
            response.setTotal(searchResult.getTotal());
            System.out.println(response);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private SearchResponse parseResult(SearchResult result){
        SearchResponse response = new SearchResponse();

        //获取所有的聚合
        MetricAggregation aggregations = result.getAggregations();
        //解析品牌的聚合结果
        //获取品牌的聚合
        TermsAggregation brandAgg = aggregations.getTermsAggregation("brandAgg");
        //获取品牌聚合中的所有桶
        List<TermsAggregation.Entry> buckets = brandAgg.getBuckets();
        //判断品牌的桶是否是空
        if(!CollectionUtils.isEmpty(buckets)){
            //初始化品牌的vo对象
            //将bucket的值转化为attrVO
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
            attrVO.setName("品牌");//写死品牌聚合的名字
            List<String> brandValues=  buckets.stream().map(bucket -> {
                HashMap<Object, Object> map = new HashMap<>();
                map.put("id", bucket.getKey());
                TermsAggregation brandNameAgg = bucket.getTermsAggregation("brandNameAgg");
                String keyAsString = brandAgg.getBuckets().get(0).getKeyAsString();
                map.put("name" , keyAsString);
                return JSON.toJSONString(map);//将String对象转化为JSON字符串
            }).collect(Collectors.toList());
                attrVO.setValue(brandValues);
                response.setBrand(attrVO);
        }

        //解析分类的结果集
        TermsAggregation categoryAgg = aggregations.getTermsAggregation("categoryAgg");
        List<TermsAggregation.Entry> catBuckets = categoryAgg.getBuckets();
        if(!CollectionUtils.isEmpty(catBuckets)){
            SearchResponseAttrVO categoryVO = new SearchResponseAttrVO();
            categoryVO.setName("分类");//将名字进行锁死
            List<String> categoryValues = catBuckets.stream().map(bucket->{
                HashMap<Object, Object> map = new HashMap<>();
                map.put("id", bucket.getKeyAsString());
                TermsAggregation categoryAggName = bucket.getTermsAggregation("categoryAggName");
                map.put("name", categoryAgg.getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);

            }).collect(Collectors.toList());
            categoryVO.setValue(categoryValues);
            response.setCatelog(categoryVO);
        }

        //解析搜索属性的聚合结果集
        ChildrenAggregation attrAgg = aggregations.getChildrenAggregation("attrAgg");
        TermsAggregation attrIdAgg = attrAgg.getTermsAggregation("attrIdAgg");
        //将桶的集合变为VO的集合
        List<SearchResponseAttrVO> attrVOS = attrIdAgg.getBuckets().stream().map(bucket->{
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
            attrVO.setProductAttributeId(Long.valueOf(bucket.getKeyAsString()));
            //获取搜索属性的子聚合（搜索属性名）
            TermsAggregation attrNameAgg = bucket.getTermsAggregation("attrNameAgg");
            attrVO.setName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            //获取搜索属性的子聚合（搜索属性值）
            TermsAggregation attrValueAgg = bucket.getTermsAggregation("attrValueAgg");
            List<String> values = attrValueAgg.getBuckets().stream().map(bucket1->
                    bucket1.getKeyAsString()
                            ).collect(Collectors.toList());
            attrVO.setValue(values);
            return attrVO;
            }).collect(Collectors.toList());
            response.setAttrs(attrVOS);
            //解析商品列表的结果集
            List<GoodsVO> goodsVOS = result.getSourceAsObjectList(GoodsVO.class , false);
            response.setProducts(goodsVOS);
            return response;
    }

    private String buildDSL(SearchParamVO searchParamVO) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //1.构建查询和过滤条件,构建一个布尔查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //构建查询条件
        String keyword = searchParamVO.getKeyword();//用户传递过来的查询条件
        //判断用户的查询条件是否为空
        if(StringUtils.isNotBlank(keyword)){
            //用来确定用户查询的条件是name，值是keyword
            boolQuery.must(QueryBuilders.matchQuery("name",keyword).operator(Operator.AND));

        }
        //构建过滤条件
        //品牌
        String[] brands = searchParamVO.getBrand();
        if(ArrayUtils.isNotEmpty(brands)){
            //构建过滤消息
            boolQuery.filter(QueryBuilders.termsQuery("brandId", brands));

        }
        //分类
        String[] catelog3 = searchParamVO.getCatelog3();
        if(ArrayUtils.isNotEmpty(catelog3)){
            boolQuery.filter(QueryBuilders.termsQuery("productCategoryId" , catelog3));

        }
        //搜索的属性过滤
        String[] props = searchParamVO.getProps();//每个属性中有多个值
        if(ArrayUtils.isNotEmpty(props)){
            for(String prop : props){
                String[] attr = StringUtils.split(prop, ":");
                if(attr != null && attr.length ==2){
                    BoolQueryBuilder propBoolQuery = QueryBuilders.boolQuery();
                    propBoolQuery.must(QueryBuilders.termQuery("attrValueList.productAttributeId", attr[0]));
                    String[] values = StringUtils.split(attr[1], "-");
                    propBoolQuery.must(QueryBuilders.termsQuery("attrValueList.value", values));
                    boolQuery.filter(QueryBuilders.nestedQuery("attrValueList", propBoolQuery, ScoreMode.None));
                }
            }
        }
        sourceBuilder.query(boolQuery);

        //2.完成分页的查询
        Integer pageNum = searchParamVO.getPageNum();
        Integer pageSize = searchParamVO.getPageSize();
        sourceBuilder.from((pageNum - 1)* pageSize);
        sourceBuilder.size(pageSize);

        //3.完成排序的构建
        String order = searchParamVO.getOrder();
        if(StringUtils.isNotBlank(order)) {
            String[] orders = StringUtils.split(order, ":");
            if (orders != null && orders.length == 2) {//判断是升序还是降序
                SortOrder sortOrder = StringUtils.equals("asc", orders[1]) ? SortOrder.ASC : SortOrder.DESC;

                switch (orders[0]) {
                    case "0"://根据score进行排序
                        sourceBuilder.sort("_score", sortOrder);
                        break;
                    case "1"://根据_sale进行排序
                        sourceBuilder.sort("_sale", sortOrder);
                        break;
                    case "2"://根据price进行排序
                        sourceBuilder.sort("price", sortOrder);
                        break;
                    default:
                        break;
                }
            }
        }


        //4.完成高亮的显示
        HighlightBuilder highlightBuilder  = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        sourceBuilder.highlighter(highlightBuilder);

        //5.完成聚合的条件查询
        //品牌
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));

        //分类
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryAgg").field("productCategoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("productCategoryName")));

        //搜索属性
        sourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg", "attrValueList")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrValueList.productAttributeId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrValueList.name"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrValueList.value"))
                        )
        );

        return sourceBuilder.toString();
    }


}
