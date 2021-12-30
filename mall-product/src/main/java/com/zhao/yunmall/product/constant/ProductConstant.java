package com.zhao.yunmall.product.constant;

/**
 * @author yuyun zhao
 * @date 2021/12/29 17:56
 */
public class ProductConstant {
	public enum AttrEnum {
		ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");
		private int code;
		private String msg;

		AttrEnum(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		public int getCode() {
			return this.code;
		}

		public String getMsg() {
			return this.msg;
		}


	}

}