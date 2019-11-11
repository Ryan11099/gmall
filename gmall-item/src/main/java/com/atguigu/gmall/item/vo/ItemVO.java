package com.atguigu.gmall.item.vo;

import com.atguigu.pms.gmall.entity.*;
import com.atguigu.pms.gmall.vo.GroupVO;
import com.atguigu.sms.gmall.vo.ItemSaleVO;
import lombok.Data;

import java.util.List;

@Data
public class ItemVO extends SkuInfoEntity {

    //下面三个在页面展示中往往处于表头部分
    private SpuInfoEntity spuInfo;
    //品牌相关的
    private BrandEntity brand;
    //分类相关的
    private CategoryEntity category;



    //图片
    private List<String> pics;
    //相关的优惠信息..由于之前封装的SaleVO 字段过多，不符合他的特性，所以我们重新进行封装
    private List<ItemSaleVO> sales;
    //判断是否有库存
    private Boolean store;
    //销售属性
    private List<SkuSaleAttrValueEntity> skuSales;
    //描述信息
    private SpuInfoDescEntity desc;
    //分组信息
    private  List<GroupVO> groups;





}
