package com.zhao.yunmall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.zhao.yunmall.product.dao.AttrAttrgroupRelationDao;
import com.zhao.yunmall.product.entity.AttrEntity;
import com.zhao.yunmall.product.service.AttrAttrgroupRelationService;
import com.zhao.yunmall.product.service.AttrService;
import com.zhao.yunmall.product.service.CategoryService;
import com.zhao.yunmall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zhao.yunmall.product.entity.AttrGroupEntity;
import com.zhao.yunmall.product.service.AttrGroupService;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.R;



/**
 * 属性分组
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 16:22:28
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 根据要查询的分类id，从属性分组表pms_attr_group中查询出等于该分类id的所有属性分组（带有模糊查询功能）
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 前端点击修改时，需要查询出选中的属性分组在数据库中的信息
     * 传入attrGroupId，根据其先查到属性分组信息，然后再根据查到的商品id递归去商品表中查找其所有父分类，设置路径后返回给前端
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
		// 获取当前属性所属商品的id
        Long catelogId = attrGroup.getCatelogId();
        // 根据该id查询出其父分类的路径（从一级到三级），注意需要使用categoryService才能查询到父id
        Long[] path = categoryService.findCatelogPath(catelogId);
        // 设置到返回对象中，传给前端
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        // 根据前端发送的属性分组查询其关联的属性
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }

    /**
     * 属性关联对话框中的删除功能：传入"attrGroupId"和"attrId"，将对应的数据从关联表中删除
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrService.deleteRelation(vos);
        return R.ok();
    }

    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params,
                            @PathVariable("attrgroupId") Long attrgroupId) {
        PageUtils page = attrService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page",page);
    }

    /**
     * 添加关联。传入{attrId, attrGroupId}数组。将二者进行关联
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        relationService.saveBatch(vos);
        return R.ok();
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
