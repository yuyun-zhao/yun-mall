package com.zhao.yunmall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yuyun.zhao
 * @createTime: 2020-06-19 11:21
 **/
@Data
public class SpuSaveVo {

    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<BaseAttrs> baseAttrs;
    private List<Skus> skus;



}
