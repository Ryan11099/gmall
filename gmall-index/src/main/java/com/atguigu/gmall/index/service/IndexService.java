package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.annoation.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.pms.gmall.entity.CategoryEntity;
import com.atguigu.pms.gmall.vo.CategoryVO;
import io.netty.util.Timeout;
import io.swagger.models.properties.PropertyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.TIMEOUT;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
//import org.redisson.api.RedissonClient;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {
    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private static final String TIMEOUT = "3000";

    private static final String KEY_PREFIX = "index:category";

    public List<CategoryEntity> queryLevellCategory() {

        Resp<List<CategoryEntity>> resp = this.gmallPmsClient.queryCategories(1, null);
        return resp.getData();
    }

    //在此处加注解就是在service中加分布式
    @GmallCache(prefix = KEY_PREFIX , timeout = 300000l , random = 50000l)
    //在此处提供了自己定义的注解，因此需要的值都可以从注解里进行获取KEY_PREFIX
    //此处注解的作用是简化了业务处理的逻辑，将获取缓存的相关业务逻辑交由注解去做
    public List<CategoryVO> queryCategoryVO( Long pid) {
        //1查询缓存。缓存中没有的话直接返回//为了区分数据所以在其中加入前缀
//        String cache = this.redisTemplate.opsForValue().get("KEY_PREFIX" + pid);
//        if(StringUtils.isNotBlank(cache)){
//            return JSON.parseArray(cache , CategoryVO.class);
//        }

        //2如果缓存中没有就查询数据库
        Resp<List<CategoryVO>> listResp = this.gmallPmsClient.queryCategoryWithSub(pid);
        List<CategoryVO> categoryVOS = listResp.getData();

        //3查询完之后。将数据放入缓存
        //3.1设置随机时间可以解决雪崩的问题
        //3.2存放空值可以解决穿透的问题
        //3.3加锁可以解决击穿的问题
//        this.redisTemplate.opsForValue().set(KEY_PREFIX+pid , JSON.toJSONString(categoryVOS) , 5+(int)(Math.random()*5) , TimeUnit.DAYS);
        return categoryVOS;
    }


    public String testLock() {

        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();

        // 获取到锁执行业务逻辑
        String numString = this.redisTemplate.opsForValue().get("num");
        if (StringUtils.isBlank(numString)) {
            return null;
        }
        int num = Integer.parseInt(numString);
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

        lock.unlock();

        return "已经增加成功";
    }
}
