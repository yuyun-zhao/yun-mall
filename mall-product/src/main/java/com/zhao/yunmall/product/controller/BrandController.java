package com.zhao.yunmall.product.controller;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.zhao.common.valid.AddGroup;
import com.zhao.common.valid.UpdateGroup;
import com.zhao.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhao.yunmall.product.entity.BrandEntity;
import com.zhao.yunmall.product.service.BrandService;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 16:22:28
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存。开启校验注解，校验前端发来的数据是否合法，如果发生异常（不合法），
     * 将直接被MallExceptionControllerAdvice拦截，其处理完后直接返回给前端
     * 所属分组：{AddGroup.class}
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand){
        // 如果合法再向服务器保存
		brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改状态信息。
     * 所属分组：{UpdateStatusGroup.class}
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated({UpdateStatusGroup.class}) @RequestBody BrandEntity brand){
		brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 修改信息
     * 所属分组：{UpdateGroup.class}
     */
    @RequestMapping("/update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand){
        // 在更新品牌数据时，需要同时更新冗余表pms_category_brand_relation中的相应品牌数据
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
