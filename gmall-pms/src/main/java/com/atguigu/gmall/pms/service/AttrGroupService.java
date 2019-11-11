package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.AttrGroupVO;
import com.atguigu.pms.gmall.vo.GroupVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性分组
 *
 * @author Ryan
 * @email Ryan@atguigu.com
 * @date 2019-10-28 19:14:30
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);


    PageVo queryByCidPage(Long catId, QueryCondition condition);

    AttrGroupVO queryGroupWithAttrs(Long gid);

    List<AttrGroupVO> queryGroupWithAttrsByCid(Long catId);

    List<GroupVO> queryGroupVOByCid(Long cid, Long spuId);

}

