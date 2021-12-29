package com.zhao.yunmall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 在 AttrEntity 的基础上，多包含了商品分类id，商品名称以及当前属性所属组的名称
 * @author yuyun zhao
 * @date 2021/12/29 15:24
 */
@Data
public class AttrResponseVo extends AttrVo {
	/**
	 * 商品分类名称
	 */
	private String catelogName;

	/**
	 * 属性分组名称
	 */
	private String groupName;

	/**
	 * 商品分组的父级路径
	 */
	private Long[] catelogPath;

}

