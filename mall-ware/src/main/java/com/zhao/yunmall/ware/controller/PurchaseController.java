package com.zhao.yunmall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.zhao.yunmall.ware.vo.MergeVo;
import com.zhao.yunmall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zhao.yunmall.ware.entity.PurchaseEntity;
import com.zhao.yunmall.ware.service.PurchaseService;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.R;



/**
 * 采购信息
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-22 13:16:39
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 查询还未被领取的采购单
     * @param params
     * @return
     */
    @GetMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPageUnreceivePurchase(params);
        return R.ok().put("page", page);
    }

    /**
     * 合并采购需求：将还未分配的多个采购需求进行合并
     * 如果选定了采购单，则将该需求合并后分配给该采购单，否则则将自动创建新的采购单
     */
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo) {
        purchaseService.mergePurchase(mergeVo);
        return R.ok();
    }


    /**
     * 领取采购单
     */
    @PostMapping("/received")
    public R receive(@RequestBody List<Long> ids) {
        purchaseService.receive(ids);
        return R.ok();
    }

    /**
     * 完成采购
     * @param doneVo
     * @return
     */
    @PostMapping("/done")
    public R finish(@RequestBody PurchaseDoneVo doneVo){

        purchaseService.done(doneVo);

        return R.ok();
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
