package com.atguigu.gmall.index.aspectj;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annoation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.naming.SelectorContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
//声明AOP 的注解
public class CacheAspect {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.gmall.index.annoation.GmallCache)")
    public Object cacheArroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable{
        //获取注解
        //.getSignature();用来获取方法相关的。getTarget();获取类相关的
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名。签名中包含了方法的许多东西
        GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);//获取注解对象
        Class returnType = signature.getReturnType();//获取方法的返回值类型
        String prefix = annotation.prefix();//获取方法的前缀
        String args = Arrays.asList(joinPoint.getArgs()).toString();//获取前缀

        //实现查询缓存
        String key = prefix + ":" + args;
        Object result = this.cacheHit(key, returnType);
        if(result !=null){
            return result;
        }

        //分布式锁
        RLock lock = this.redissonClient.getLock("lock" + args);
        lock.lock();
        //环绕通知在此语句的前后进行书写

        //查询缓存
        result = this.cacheHit(key, returnType);
        if (result!=null){
            lock.unlock();
            return result;
        }
        //****这里的joinpoint就是需要执行的方法的方法名，也就是IndexService中的queryCategoryVO****//
        result = joinPoint.proceed(joinPoint.getArgs());

        //实现放入缓存
        long timeout = annotation.timeout();
        timeout = timeout + (long) (Math.random() * annotation.random());
        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result) , timeout , TimeUnit.SECONDS);
        lock.unlock();
        return result;
    }

    //命中缓存的实现
    private Object cacheHit(String key , Class returnType){
        String jsonString = this.redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(jsonString)){
            return JSON.parseObject(jsonString , returnType);
        }
        return null;

    }
}
