package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * sku销售属性&值
 *
 * @author Ryan
 * @email Ryan@atguigu.com
 * @date 2019-10-28 19:14:30
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageVo queryPage(QueryCondition params);

    List<SkuSaleAttrValueEntity> querySaleAttrValues(Long spuId);
}

