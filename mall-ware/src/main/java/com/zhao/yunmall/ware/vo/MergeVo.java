package com.zhao.yunmall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author yuyun zhao
 * @date 2021/12/31 14:39
 */
@Data
public class MergeVo {
	private Long purchaseId;
	private List<Long> items;
}
