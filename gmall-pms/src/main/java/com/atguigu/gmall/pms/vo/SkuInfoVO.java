package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuInfoVO extends SkuInfoEntity {

    //这里的VO里的字段都是我们需要的字段，但是在数据传输时我们只能将数据分卡传送，因此
    //设置一个VO 来接受这些VO对象。由于这里已经继承了本来该有的对象，因此只需要把没有的
    //对象添加进去即可



    //这些多余的属性都是在其他的表中，因此要跨服务访问

    // 购物积分相关字段
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */
    private List<Integer> work;

    // 打折优惠
    private Integer fullCount;
    /**
     * 打几折
     */
    private BigDecimal discount;
    /**
     * 是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
    private Integer ladderAddOther;

    // 满减优惠
    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;
    /**
     * 是否参与其他优惠
     */
    private Integer fullAddOther;

    // 销售属性
    private List<SkuSaleAttrValueEntity> saleAttrs;

    private List<String> images;
}
