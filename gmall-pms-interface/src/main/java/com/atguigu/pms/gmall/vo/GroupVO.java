package com.atguigu.pms.gmall.vo;

import com.atguigu.pms.gmall.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

@Data
public class GroupVO {
    private String groupName;

    private List<ProductAttrValueEntity> baseAttrValues;
}
