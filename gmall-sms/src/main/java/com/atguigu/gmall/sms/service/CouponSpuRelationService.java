package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.CouponSpuRelationEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 优惠券与产品关联
 *
 * @author Ryan
 * @email Ryan@atguigu.com
 * @date 2019-10-28 19:26:58
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageVo queryPage(QueryCondition params);
}

