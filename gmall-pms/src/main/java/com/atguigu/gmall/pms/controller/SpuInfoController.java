package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.service.SpuInfoService;




/**
 * spu信息
 *
 * @author Ryan
 * @email Ryan@atguigu.com
 * @date 2019-10-28 19:14:30
 */
@Api(tags = "spu信息 管理")
@RestController
@RequestMapping("pms/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @GetMapping
    public Resp<PageVo> querySpuInfoByKeyPage(@RequestParam(value = "catId" , defaultValue = "0")Long catId , QueryCondition condition ){
        PageVo pageVo = spuInfoService.querySpuInfoByKeyPage(catId , condition);
        return Resp.ok(pageVo);
    }

    /**
     * 列表
     */

    @ApiOperation("分页查询(排序)")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('pms:spuinfo:list')")
    public  Resp<List<SpuInfoEntity>> querySpuPage(@RequestBody QueryCondition queryCondition) {
        PageVo page = spuInfoService.queryPage(queryCondition);

        return Resp.ok((List<SpuInfoEntity>)page.getList());
    }



    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:spuinfo:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = spuInfoService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAuthority('pms:spuinfo:info')")
    public Resp<SpuInfoEntity> info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return Resp.ok(spuInfo);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:spuinfo:save')")
    public Resp<Object> save(@RequestBody SpuInfoVO spuInfoVO){
		spuInfoService.bigSave(spuInfoVO);
        //这里的保存已经不是对于单表的操作了，因为要保存的数据涉及到了好几张表
        return Resp.ok(null);
    }


//    @ApiOperation("保存")
//    @PostMapping("/save")
//    @PreAuthorize("hasAuthority('pms:spuinfo:save')")
//    public Resp<Object> save(@RequestBody SpuInfoEntity spuInfoEntity){
//        spuInfoService.save(spuInfoEntity);
//        //这里的保存已经不是对于单表的操作了，因为要保存的数据涉及到了好几张表
//        return Resp.ok(null);
//    }


    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:spuinfo:update')")
    public Resp<Object> update(@RequestBody SpuInfoEntity spuInfo){
        spuInfoService.updateById(spuInfo);
        this.sendMsg(spuInfo.getId(), "update");
        return Resp.ok(null);
    }

    private void sendMsg(Long spuId, String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", spuId);
        map.put("type", type);
        this.amqpTemplate.convertAndSend("GMALL-ITEM-EXCHANGE", "item." + type, map);
    }
    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:spuinfo:delete')")
    public Resp<Object> delete(@RequestBody Long[] ids){
        System.out.println("111111111111111");
		spuInfoService.removeByIds(Arrays.asList(ids));

        return Resp.ok(null);
    }

}
