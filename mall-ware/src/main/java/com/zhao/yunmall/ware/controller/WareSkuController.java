package com.zhao.yunmall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.zhao.common.exception.BizCodeEnum;
import com.zhao.common.exception.NoStockException;
import com.zhao.yunmall.ware.vo.SkuHasStockVo;
import com.zhao.yunmall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zhao.yunmall.ware.entity.WareSkuEntity;
import com.zhao.yunmall.ware.service.WareSkuService;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.R;



/**
 * 商品库存
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-22 13:16:39
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 查询sku是否有库存
     */
    @PostMapping("/getSkuHasStock")
    public List<SkuHasStockVo> getSkusHasStock(@RequestBody List<Long> skuIds) {
        return wareSkuService.getSkuHasStock(skuIds);
    }
    /**
     * 下订单时锁库存
     * @param lockVo
     * @return
     */
    @RequestMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo lockVo) {
        try {
            Boolean lock = wareSkuService.orderLockStock(lockVo);
            return R.ok();
        } catch (NoStockException e) {
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(), BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
