package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {

        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<>();

        System.out.println(type);
        switch (type){
            case 1: wrapper.eq("username", data);break;
            case 2: wrapper.eq("mobile", data);break;
            case 3: wrapper.eq("email", data);break;
            default: return false;
        }
        return this.count(wrapper) == 0;
        //意思是按照查询条件进行查询的有几条，如果查到了就返回false
    }

    @Override
    public void register(MemberEntity memberEntity) {
        //1.校验验证码

        //2.生成盐

        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        memberEntity.setSalt(salt);
        //3.加盐加密
        memberEntity.setPassword(DigestUtils.md5Hex(memberEntity.getPassword() + salt));

        //4.注册功能
        memberEntity.setLevelId(1l);
        memberEntity.setStatus(1);
        memberEntity.setCreateTime(new Date());
        memberEntity.setIntegration(0);
        memberEntity.setGrowth(0);
        this.save(memberEntity);

        //5.删除redis中的验证码

    }

    @Override
    public MemberEntity queryUser(String userName, String password) {

        //因为要查询的密码是经过加盐加密之后的，所以我们先要对密码进行加盐加密
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", userName));

        //判断获取到的对象是否为空
        if(memberEntity == null){
            throw new IllegalArgumentException("用户名不合法");
        }

        //对用户的密码进行加密
        DigestUtils.md5Hex(password + memberEntity.getSalt());

        if (!StringUtils.equals(password, memberEntity.getPassword())){
            throw new IllegalArgumentException("密码不合法");
        }
        return memberEntity;

    }

}