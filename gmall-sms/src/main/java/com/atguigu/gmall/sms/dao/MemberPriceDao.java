package com.atguigu.gmall.sms.dao;

import com.atguigu.gmall.sms.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author Ryan
 * @email Ryan@atguigu.com
 * @date 2019-10-28 19:26:58
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
