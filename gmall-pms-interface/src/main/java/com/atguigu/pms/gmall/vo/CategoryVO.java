package com.atguigu.pms.gmall.vo;

import com.atguigu.pms.gmall.entity.CategoryEntity;
import lombok.Data;

import java.util.List;
@Data
public class CategoryVO extends CategoryEntity {

    private List<CategoryEntity> subs;

}
