package com.zhao.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yuyun zhao
 * @date 2021/12/30 16:37
 */
@Data
public class SpuBoundTo {
	private Long spuId;
	private BigDecimal buyBounds;
	private BigDecimal grouwBounds;
}
