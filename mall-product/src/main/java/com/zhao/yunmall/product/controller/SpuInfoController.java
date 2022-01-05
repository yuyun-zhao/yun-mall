package com.zhao.yunmall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.zhao.yunmall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zhao.yunmall.product.entity.SpuInfoEntity;
import com.zhao.yunmall.product.service.SpuInfoService;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.R;



/**
 * spu信息
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 16:22:28
 */
@RestController
@RequestMapping("/product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 查询：根据商品分类id，品牌分类id以及发布状态等信息，查询SPU信息
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }

    /**
     * 上架商品，将选中的商品添加到ES中
     */
    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId") Long spuId) {
        spuInfoService.up(spuId);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 新增商品时，前端会传来一个非常大的JSON数据，里面包含了SPU的各种信息
     * 该方法用于解析JSON字符串里的这些参数并分别保存到对应的数据库里
     */
    @RequestMapping("/save")
    public R save(@RequestBody SpuSaveVo spuInfoVo){
		spuInfoService.saveSpuInfo(spuInfoVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
