package com.atguigu.gmall.item.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gamll.wms.entity.WareSkuEntity;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.pms.gmall.entity.*;
import com.atguigu.pms.gmall.vo.GroupVO;
import com.atguigu.sms.gmall.vo.ItemSaleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;

    public ItemVO item(Long skuId) {
        ItemVO itemVO = new ItemVO();

        //1.查询sku的信息
        //要获取数据先要获取sku的全部信息
        Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(skuId);
        SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
        BeanUtils.copyProperties(skuInfoEntity, itemVO);


        //2.根据sku的信息来设置品牌
        Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
        itemVO.setBrand(brandEntityResp.getData());


        //3.设置分类
        Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
        itemVO.setCategory(categoryEntityResp.getData());


        //4.设置spu的信息
        Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuById(skuInfoEntity.getSpuId());
        itemVO.setSpuInfo(spuInfoEntityResp.getData());


        //5.设置图片信息
        Resp<List<String>> listResp = this.gmallPmsClient.queryPicsBySkuId(skuId);
        itemVO.setPics(listResp.getData());


        //6.设置营销信息
        Resp<List<ItemSaleVO>> itemSaleResp = this.gmallSmsClient.queryItemSaleVOs(skuId);
        itemVO.setSales(itemSaleResp.getData());


        //7.是否有货
        Resp<List<WareSkuEntity>> queryWareBySkuId = this.gmallWmsClient.queryWareBySkuId(skuId);
        //只要有任何一家有货即判断有货
        List<WareSkuEntity> wareSkuEntities = queryWareBySkuId.getData();
        boolean b = wareSkuEntities.stream().anyMatch(t -> t.getStock() > 0);//只要判断有任何一家的库存大于0，即为true
        itemVO.setStore(b);


        //8.spu的全部销售属性
        Resp<List<SkuSaleAttrValueEntity>> skuSaleAttrValueResp = this.gmallPmsClient.querySaleAttrValues(skuInfoEntity.getSpuId());
        itemVO.setSkuSales(skuSaleAttrValueResp.getData());


        //9.设置spu的描述信息
        Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescById(skuInfoEntity.getSpuId());
        itemVO.setDesc(spuInfoDescEntityResp.getData());


        //10.设置规格属性分组以及规格参数及值
        Resp<List<GroupVO>> listResp1 = this.gmallPmsClient.queryGroupVOByCid(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
        itemVO.setGroups(listResp1.getData());



        return itemVO;
    }
}
