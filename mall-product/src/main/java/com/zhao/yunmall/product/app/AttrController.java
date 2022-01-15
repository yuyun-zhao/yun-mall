package com.zhao.yunmall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.zhao.yunmall.product.vo.AttrResponseVo;
import com.zhao.yunmall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zhao.yunmall.product.service.AttrService;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.R;



/**
 * 商品属性
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 16:22:28
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    /**
     * 当前端点击左侧三级菜单的某个商品种类时，发出该请求
     * 根据指定的商品分类id查询该分类对应的属性参数（分为两种："base" 规格参数查询  "sale" 销售参数查询）
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("attrType") String type,
                          @PathVariable("catelogId") Long catelogId) {
        // 如果传入的catelogId == 0，代表全部查询，否则就为条件查询
        PageUtils page = attrService.queryBaseAttrPage(params, type, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 根据传入的属性id，查询出该属性的全部信息以及该属性对应的商品的全父级路径
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
        AttrResponseVo respVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", respVo);
    }

    /**
     * 新增：从前端收集属性参数（包含分组信息），封装到AttrVo对象中
     * 保存属性值到属性表，并且保存属性信息到关联表
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);
        return R.ok();
    }

    /**
     * 前端发出保存请求，将用户修改后的属性信息、分组信息、商品信息进行保存
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);
        return R.ok();
    }



    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
