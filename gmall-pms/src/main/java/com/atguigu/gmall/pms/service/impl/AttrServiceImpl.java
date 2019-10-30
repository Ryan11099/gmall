package com.atguigu.gmall.pms.service.impl;


import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryAttrByCid(Integer type, Long cid, QueryCondition condition) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();


        if (type != null) {
            wrapper.eq("attr_type", type);
        }
        if (cid != null) {
            wrapper.eq("catelog_id", cid);
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(condition),
                wrapper
                );
        return new PageVo(page);

    }

    @Transactional
    //加事务注解因为要操作两张表。要么共同成功，要么共同失败
    @Override
    public void saveAttrAndRelation(AttrVo attrVo) {

        //因为要操作两张表，所以要分为两步来实现，先给一张表插入数据。再给另一张表掺入数据
        //插入attr
        this.save(attrVo);

        //插入中间表
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
        relationEntity.setAttrId(attrVo.getAttrId());
        relationEntity.setAttrSort(0);
        this.relationDao.insert(relationEntity);

    }

}