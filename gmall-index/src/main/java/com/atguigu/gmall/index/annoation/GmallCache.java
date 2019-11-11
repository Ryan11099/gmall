package com.atguigu.gmall.index.annoation;

import org.springframework.core.annotation.AliasFor;

import javax.xml.bind.annotation.XmlType;
import java.lang.annotation.*;

@Target(ElementType.METHOD)//可以使用该注解的类型
@Retention(RetentionPolicy.RUNTIME)//运行时的注解
@Documented//是否有文档进行添加
public @interface GmallCache {
    /**
     * 过期时间的范围，为了防止雪崩事件的发生
     * @return
     */
    long random() default 300l;

    /**
     * 设置的过期时间
     * @return
     */
    long timeout() default 300l;

    /**
     * 设置的默认的前缀
     * @return
     */

    String prefix() default "cache";
}
