package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SeckillPromotionEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 秒杀活动
 *
 * @author Ryan
 * @email Ryan@atguigu.com
 * @date 2019-10-28 19:26:58
 */
public interface SeckillPromotionService extends IService<SeckillPromotionEntity> {

    PageVo queryPage(QueryCondition params);
}

