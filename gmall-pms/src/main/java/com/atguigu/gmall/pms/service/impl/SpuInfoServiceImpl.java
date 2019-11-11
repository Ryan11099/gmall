package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.*;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.SpuInfoDescService;
import com.atguigu.gmall.pms.vo.ProductAttrValueVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;

import java.util.*;

import com.atguigu.sms.gmall.vo.SaleVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao descDao;
    @Autowired
    private ProductAttrValueDao productAttrValueDao;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesDao skuImagesDao;
    @Autowired
    private SkuSaleAttrValueDao saleAttrValueDao;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private SpuInfoDao spuInfoDao;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuInfoByKeyPage(Long catId, QueryCondition condition) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        //wrapper后面是查询条件，所需的查询条件自己来进行拼接
        if (catId != null) {
            wrapper.eq("catalog_id" , catId);
        }
        //这里的过程既是Mybatis plus的查询过程。
        // 将浏览器查询的条件转化为MP所能识别的字段，再将查询出来的字段进行转化成为页面所需的字段进行封装成为Ipage对象
        String key = condition.getKey();
        if (StringUtils.isNotBlank(key)) {
            //这里的查询是将模糊查询与根据id查询进行结合来查询的.
            //and后面是函数式编程
            //意思是要么查询id相等的，要么根据 spu_name 进行模糊查询
            wrapper.and(t->t.eq("id", key).or().like("spu_name" , key));

        }
            //Ipage是查询出来的对象进行的封装
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );
            //将Ipage对象进行转化为页面所需要显示的PageVo对象
        return new PageVo(page);
    }

    @GlobalTransactional
    @Override
   // @Transactional
    public void bigSave(SpuInfoVO spuInfoVO) {
        //因为操作的有九张表。spu有三张，sku有三张，营销有三张

        //1.新增spu相关的三张表

        //1.1新增spuInfo
        Long spuId = saveSpuInfo(spuInfoVO);
        //1.2新增spuInfoDesc,新增的顺序不能变，因为这个的增加需要使用到spuIfoid
        //saveSpuInfoDesc(spuInfoVO, spuId);
        this.spuInfoDescService.saveSpuDesc(spuInfoVO, spuId);
        //1.3新增基本属性
        saveBaseAttr(spuInfoVO, spuId);
        //2.新增sku相关的三张表

        //2.1获取sku相关信息
        saveSku(spuInfoVO , spuId);

        //在执行的最后进行发送消息说明有消息需要进行同步

        sendMsg(spuId , "insert");//需要在传输时直接确定是什么类型的操作

        //int i= 1/0;
    }
    //进行消息同步的方法
    private void sendMsg(Long spuId , String type) {
        Map<Object, Object> map = new HashMap<>();
        map.put("id", spuId);
        map.put("type", type);
        this.amqpTemplate.convertAndSend("GMALL-ITEM-EXCHANGE", "item."+type,map );
    }

    private void saveSku(SpuInfoVO spuInfoVO ,Long spuId) {
        //在整个执行前，先判断sku的信息是否有，如果没有直接返回。如果有再进行以下操作
        List<SkuInfoVO> skus = (List<SkuInfoVO>) spuInfoVO.getSkus();
        if(CollectionUtils.isEmpty(skus)){
            return;
        }

        //2.2新增skuInfo
        //因为后面的所有的数据都与sku 有关，则所有的操作都需要在遍历下进行
        //因为浏览器传递过来的sku信息都是集合类型的
        for (SkuInfoVO skuInfoVO : skus) {
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            //所有的信息都传给了Entity
            //因为在一个项目组中肯定不止一个人在使用这个信息，如果你的电脑上修改了，那么其他人如何使用
            //所以在这里，重新new一个对象专门用来进行传输作用
            BeanUtils.copyProperties(skuInfoVO, skuInfoEntity);
            //**********************11**********************//
            skuInfoEntity.setBrandId(spuInfoVO.getBrandId());
            skuInfoEntity.setCatalogId(spuInfoVO.getCatalogId());
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString());
            skuInfoEntity.setSpuId(spuId);
            //***********************11*********************//
            //上面是将属性中没有的进行设置
            List<String> images = skuInfoVO.getImages();//可以上传多个图片
            //设置默认图片，既是第一次上传的图片
            //要对图片进行判断，如果有传就用传的，如果没有传，就用默认的第一个
            if (!CollectionUtils.isEmpty(images)) {
                skuInfoEntity.setSkuDefaultImg
                        (StringUtils.isNotBlank(skuInfoEntity.getSkuDefaultImg())
                                ? skuInfoEntity.getSkuDefaultImg() : images.get(0));
            }
            //所以最后保存的是Entity
            this.skuInfoDao.insert(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();

            // 2.3. 新增sku的图片
            if (!CollectionUtils.isEmpty(images)) {
                images.forEach(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    //设置默认图片设置第一张为默认图片，判断是否为第一张
                    skuImagesEntity.setDefaultImg(StringUtils.equals(image, skuInfoEntity.getSkuDefaultImg()) ? 1 : 0);
                    skuImagesEntity.setImgSort(0);
                    skuImagesEntity.setImgUrl(image);
                    this.skuImagesDao.insert(skuImagesEntity);
                });
            }
            //2.4 新增销售属性
            //销售信息也要遍历产生
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {//如果saleAttrs不为空。则对其进行遍历
                saleAttrs.forEach(saleAttr -> {
                    saleAttr.setSkuId(skuId);
                    saleAttr.setAttrSort(0);
                    this.saleAttrValueDao.insert(saleAttr);
                });
            }

            //3.新增营销的三张表
            System.out.println("3333333333333");
            SaleVO saleVO = new SaleVO();
            BeanUtils.copyProperties(skuInfoVO, saleVO);
            saleVO.setSkuId(skuId);
            this.smsClient.saveSale(saleVO);
        }
    }

    private void saveBaseAttr(SpuInfoVO spuInfoVO, Long spuId) {
        List<ProductAttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        baseAttrs.forEach(baseAttr -> {
            baseAttr.setSpuId(spuId);
            baseAttr.setAttrSort(0);
            baseAttr.setQuickShow(1);
            this.productAttrValueDao.insert(baseAttr);
        });
        System.out.println("222222222222");
    }
   /* private void saveSpuInfoDesc(SpuInfoVO spuInfoVO, Long spuId) {
        List<String> spuImages = spuInfoVO.getSpuImages();
        String desc = StringUtils.join(spuImages, ",");
        //join函数的作用，将集合类型转化为字符串类型
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuId);
        descEntity.setDecript(desc);
        this.descDao.insert(descEntity);
    }*/
    private Long saveSpuInfo(SpuInfoVO spuInfoVO) {
        System.out.println("111111111111111");
        spuInfoVO.setCreateTime(new Date());
        System.out.println("初始化数据"+new Date());
        //两者之间使用同一个数据，如果没有使用同一个数据，会有毫秒的延迟，导致时间不准确
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        System.out.println("新数据"+spuInfoVO.getCreateTime());
        this.save(spuInfoVO);
        //这里的没有使用Dao是因为在本对象的下面
        return spuInfoVO.getId();
    }

}