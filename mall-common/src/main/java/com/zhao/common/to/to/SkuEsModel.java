package com.zhao.common.to.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 传输对象，存储到es的数据
 * @author yuyun zhao
 * @date 2022/1/5 17:39
 */
@Data
public class SkuEsModel {
	/**
	 * 后台管理系统只传来 spuId，将根据该值查询得到下面的其他信息
	 */
	private Long spuId;

	/**
	 * sku 信息，从 pms_sku_info 表中查询
	 */
	private Long skuId;
	private String skuTitle;
	private BigDecimal skuPrice;
	private String skuImg;
	private Long saleCount;

	/**
	 * 是否还有库存，远程调用库存服务（从 wms_ware_sku 表中查询）
	 */
	private Boolean hasStock;

	/**
	 * 评分热度，未来扩展
	 */
	private Long hotScore;

	/**
	 *
	 */
	private Long catalogId;
	private String catalogName;

	/**
	 * 品牌的信息，从 pms_brand 表中查询
 	 */
	private Long brandId;
	private String brandName;
	private String brandImg;

	/**
	 * 商品的属性值，从 pms_attr 表中查询
	 */
	private List<Attrs> attrs;

	@Data
	public static class Attrs {
		private Long attrId;
		private String attrName;
		private String attrValue;
	}
}
