package com.zhao.yunmall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.zhao.common.valid.AddGroup;
import com.zhao.common.valid.ListValue;
import com.zhao.common.valid.UpdateGroup;
import com.zhao.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 16:22:28
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌。使用分组校验，分情况决定是否为null。新增时必须为空；修改时不能为空
	 */
	@TableId
	@NotNull(message = "修改时必须指定品牌id", groups = {UpdateGroup.class})
	@Null(message = "新增时不能指定品牌id", groups = {AddGroup.class})
	private Long brandId;
	/**
	 * 品牌名。任何情况都要校验：至少得是一个非空格字符
	 */
	@NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址。新增时需要校验：不能为空且必须是合法URL；修改时可以为空，如果不为空时要得是合法URL
	 */
	@NotEmpty(groups = {AddGroup.class})
	@URL(message = "log必须是一个合法的url地址", groups = {AddGroup.class,UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 * 新增时不能为空，修改状态时不能为空。普通修改可以为空
	 */
	//@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
	@ListValue(vals = {0, 1}, groups = {AddGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母。新增时不能为空，修改时可以为空；如果不为空时必须得符合正则
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class,UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序。新增时不能为空，修改时可以为空；如果不为空则必须要满足大于0
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0, message = "排序必须大于等于0", groups = {AddGroup.class,UpdateGroup.class})
	private Integer sort;

}
