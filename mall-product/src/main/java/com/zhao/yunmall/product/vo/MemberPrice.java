/**
  * Copyright 2019 bejson.com 
  */
package com.zhao.yunmall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yuyun.zhao
 * @createTime: 2020-06-19 11:21
 **/
@Data
public class MemberPrice {

    private Long id;
    private String name;
    private BigDecimal price;

}
