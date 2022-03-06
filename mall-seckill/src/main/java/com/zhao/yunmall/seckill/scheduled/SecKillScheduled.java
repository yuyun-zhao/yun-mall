package com.zhao.yunmall.seckill.scheduled;

import com.zhao.yunmall.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 开启【定时任务】+【异步任务】
 */
@Slf4j
@EnableAsync
@EnableScheduling
@Component
public class SecKillScheduled {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SecKillService secKillService;

    //秒杀商品上架功能的锁
    private final String upload_lock = "seckill:upload:lock";

    /**
     * 定时任务
     * 每天三点上架最近三天的秒杀商品
     * 幂等性：上架后就不需要再上架了
     */
    @Async
    //@Scheduled(cron = "0 0 3 * * ?")
   @Scheduled(cron = "*/30 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        // 为避免分布式情况下多服务同时上架的情况，使用分布式锁
        RLock lock = redissonClient.getLock(upload_lock);
        try {
            lock.lock(10, TimeUnit.SECONDS);
            log.info("上架秒杀的商品信息...");
            secKillService.uploadSeckillSkuLatest3Days();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}
