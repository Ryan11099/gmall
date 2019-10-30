package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.dao.BrandDao;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GmallPmsApplicationTests {

    @Autowired
   private BrandDao brandDao;

    @Autowired
    private BrandService brandService;
    @Test
    void contextLoads() {
    }
    @Test
    public void test(){

//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setDescript("齐天大圣");
//        brandEntity.setLogo("www.lohu.123.gif");
//        brandEntity.setName("老母鸡");
//        brandEntity.setFirstLetter("j");
//        this.brandDao.insert(brandEntity);
        //this.brandDao.deleteById(6);
//        Map<String , Object> map = new HashMap<>();
//        map.put("name", "老母鸡");
//        this.brandDao.selectByMap(map);

        IPage<BrandEntity> page = this.brandService.page(new Page<BrandEntity>(1l, 2l), new QueryWrapper<BrandEntity>().eq("sort", 1));
        System.out.println(page.getRecords());
        System.out.println(page.getTotal());
        System.out.println(page.getPages());
    }
}
