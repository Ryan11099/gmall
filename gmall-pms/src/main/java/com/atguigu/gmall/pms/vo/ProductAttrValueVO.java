package com.atguigu.gmall.pms.vo;


import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

public class ProductAttrValueVO extends ProductAttrValueEntity {

    public void setValueSelected(List<String> valueSelected){

//        if(CollectionUtils.isEmpty(valueSelected)){
//            return;
//        }

        this.setAttrValue(StringUtils.join(valueSelected , ","));
        //StringUtils的join方法可以将集合对象转化为字符串
    }
    //此处可以不加@data因为此处我们就写了一个set的方法
}
