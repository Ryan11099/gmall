package com.atguigu.gmall.ums.service;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 会员
 *
 * @author Ryan
 * @email Ryan@atguigu.com
 * @date 2019-10-28 19:30:13
 */
public interface MemberService extends IService<MemberEntity> {

    PageVo queryPage(QueryCondition params);

    Boolean checkData(String data, Integer type);

    void register(MemberEntity memberEntity);

    MemberEntity queryUser(String userName, String password);

}

