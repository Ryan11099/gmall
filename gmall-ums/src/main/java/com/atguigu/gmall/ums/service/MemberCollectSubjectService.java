package com.atguigu.gmall.ums.service;

import com.atguigu.gmall.ums.entity.MemberCollectSubjectEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 会员收藏的专题活动
 *
 * @author Ryan
 * @email Ryan@atguigu.com
 * @date 2019-10-28 19:30:13
 */
public interface MemberCollectSubjectService extends IService<MemberCollectSubjectEntity> {

    PageVo queryPage(QueryCondition params);
}

