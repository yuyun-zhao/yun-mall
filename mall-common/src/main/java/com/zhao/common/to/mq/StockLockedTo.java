package com.zhao.common.to.mq;

import lombok.Data;

@Data
public class StockLockedTo {
    private Long id;
    private StockDetailTo detailTo;
}
