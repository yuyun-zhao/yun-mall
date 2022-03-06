package com.zhao.yunmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import com.zhao.common.utils.R;
import com.zhao.yunmall.seckill.feign.CouponFeignService;
import com.zhao.yunmall.seckill.feign.ProductFeignService;
import com.zhao.yunmall.seckill.service.SecKillService;
import com.zhao.yunmall.seckill.to.SeckillSkuRedisTo;
import com.zhao.yunmall.seckill.vo.SeckillSessionWithSkusVo;
import com.zhao.yunmall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service("SecKillService")
public class SecKillServiceImpl implements SecKillService {

	@Autowired
	private CouponFeignService couponFeignService;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ProductFeignService productFeignService;

	@Autowired
	private RedissonClient redissonClient;

	// @Autowired
	// private RabbitTemplate rabbitTemplate;

	// 	// Redis 里保存的是 场次Id-SkuId，这样能保证不同场次的相同sku也能正常上架


	// List 结构：秒杀活动的信息 seckill:sessions:startTime_endTime : [sessionId01-skuId01, sessionId02-skuId02, ...]
	// 例如：key:   seckill:sessions:1646496000000_1646499600000
	//      value: [1-1, 1-2, ...]
	//K: SESSION_CACHE_PREFIX + startTime + "_" + endTime
	//V: sessionId+"-"+skuId的List
	private final String SESSION_CACHE_PREFIX = "seckill:sessions:";

	// Hash 结构：秒杀活动的商品信息 seckill:skus ：Map[sessionId01-skuId01:SeckillSkuRedisTo, sessionId02-skuId02:v为对应的商品信息SeckillSkuRedisTo..]
	// 例如： key:   seckill:skus
	//       value:  [1-2:"...json.."; 1-1:"...json...";...]
	//K: 固定值SECKILL_CHARE_PREFIX
	//V: hash，k为sessionId+"-"+skuId，v为对应的商品信息SeckillSkuRedisTo
	private final String SECKILL_CHARE_PREFIX = "seckill:skus";

	// String 类型：seckill:stock: ：uuid
	// 例如：seckill:stock:48a82e953f2949f388e267f7b277ef1f
	//K: SKU_STOCK_SEMAPHORE+商品随机码
	//V: 秒杀的库存件数
	private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";    //+商品随机码


	@Override
	public void uploadSeckillSkuLatest3Days() {
		// 远程调用会员服务获取最近三天内的要秒杀的SKU信息
		R r = couponFeignService.getSeckillSessionsIn3Days();
		if (r.getCode() == 0) {
			// 上架商品

			// 1. 解析出sku信息
			String seckillSessions = JSONObject.toJSONString(r.get("seckillSessions"));
			List<SeckillSessionWithSkusVo> sessions =
					JSON.parseObject(seckillSessions, new TypeReference<List<SeckillSessionWithSkusVo>>() {
					});

			// 2. 在Redis中分别缓存秒杀活动场次信息和活动场次关联的商品SKU信息
			// 2.1 缓存活动信息
			saveSecKillSession(sessions);
			// 2.2 缓存活动关联的商品信息
			saveSecKillSku(sessions);
		}
	}

	/**
	 * 前端发出 ajax 请求查询当前可以秒杀的商品
	 * 从 Redis 里查询当前可以参与秒杀的商品信息 List<SeckillSkuRedisTo>
	 * @return
	 */
	@Override
	public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
		// 1. 确定当前系统时间属于哪个秒杀场次
		// 2. 回去这个秒杀场次需要的所有商品信息
		// 模糊匹配所有符合 seckill:sessions: 前缀的数据
		// 这里其实可以用 SCAN 代替，渐进式的批量查询
		// 使用方式：https://my.oschina.net/xiaolyuh/blog/3169203
		// keys：当数据量大的时候，会阻塞 Redis 主线程，导致可能会积压大量请求导致服务雪崩
		// 服务雪崩也可以举着个例子？

	    Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
	    long currentTime = System.currentTimeMillis();
	    for (String key : keys) {
	    	// 把查询到的每一个key都进行截取，找出开始时间和结束时间，与系统当时时间比对，判断是否在当前区间（最近三天）内
	        String replace = key.replace(SESSION_CACHE_PREFIX, "");
	        String[] split = replace.split("_");
	        long startTime = Long.parseLong(split[0]);
	        long endTime = Long.parseLong(split[1]);
	        // 如果当前秒杀活动处于有效期内
	        if (currentTime > startTime && currentTime < endTime) {
	        	// 在List类型的value中进行范围查询，查出所有数据
				// 即把当前时间段内的所有“场次id-skuId”信息找了出来（存储在List里）
	            List<String> range = redisTemplate.opsForList().range(key, -100, 100);
	            // 去 hash 结构的商品信息里寻找每一个场次-商品 对应的sku信息（返回给前端）
	            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);

	            List<SeckillSkuRedisTo> collect = range.stream().map(s -> {
	            	// 在 hash 结构的商品信息里寻找当前场次-商品id 对应的详细sku信息
	                String json = (String) ops.get(s);
	                SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
	                return redisTo;
	            }).collect(Collectors.toList());
	            return collect;
	        }
	    }
	    return null;
	}

	@Override
	public SeckillSkuRedisTo getSeckillSkuInfo(Long skuId) {
	    BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
	    Set<String> keys = ops.keys();
	    for (String key : keys) {
	        if (Pattern.matches("\\d-" + skuId,key)) {
	            String v = ops.get(key);
	            SeckillSkuRedisTo redisTo = JSON.parseObject(v, SeckillSkuRedisTo.class);
	            //当前商品参与秒杀活动
	            if (redisTo!=null){
	                long current = System.currentTimeMillis();
	                //当前活动在有效期，暴露商品随机码返回
	                if (redisTo.getStartTime() < current && redisTo.getEndTime() > current) {
	                    return redisTo;
	                }
	                redisTo.setRandomCode(null);
	                return redisTo;
	            }
	        }
	    }
	    return null;
	}

	// @Override
	// public String kill(String killId, String key, Integer num) throws InterruptedException {
	//     BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
	//     String json = ops.get(killId);
	//     String orderSn = null;
	//     if (!StringUtils.isEmpty(json)){
	//         SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
	//         //1. 验证时效
	//         long current = System.currentTimeMillis();
	//         if (current >= redisTo.getStartTime() && current <= redisTo.getEndTime()) {
	//             //2. 验证商品和商品随机码是否对应
	//             String redisKey = redisTo.getPromotionSessionId() + "-" + redisTo.getSkuId();
	//             if (redisKey.equals(killId) && redisTo.getRandomCode().equals(key)) {
	//                 //3. 验证当前用户是否购买过
	//                 MemberResponseVo memberResponseVo = LoginInterceptor.loginUser.get();
	//                 long ttl = redisTo.getEndTime() - System.currentTimeMillis();
	//                 //3.1 通过在redis中使用 用户id-skuId 来占位看是否买过
	//                 Boolean occupy = redisTemplate.opsForValue().setIfAbsent(memberResponseVo.getId()+"-"+redisTo.getSkuId(), num.toString(), ttl, TimeUnit.MILLISECONDS);
	//                 //3.2 占位成功，说明该用户未秒杀过该商品，则继续
	//                 if (occupy){
	//                     //4. 校验库存和购买量是否符合要求
	//                     if (num <= redisTo.getSeckillLimit()) {
	//                         //4.1 尝试获取库存信号量
	//                         RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisTo.getRandomCode());
	//                         boolean acquire = semaphore.tryAcquire(num,100,TimeUnit.MILLISECONDS);
	//                         //4.2 获取库存成功
	//                         if (acquire) {
	//                             //5. 发送消息创建订单
	//                             //5.1 创建订单号
	//                             orderSn = IdWorker.getTimeId();
	//                             //5.2 创建秒杀订单to
	//                             SeckillOrderTo orderTo = new SeckillOrderTo();
	//                             orderTo.setMemberId(memberResponseVo.getId());
	//                             orderTo.setNum(num);
	//                             orderTo.setOrderSn(orderSn);
	//                             orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
	//                             orderTo.setSeckillPrice(redisTo.getSeckillPrice());
	//                             orderTo.setSkuId(redisTo.getSkuId());
	//                             //5.3 发送创建订单的消息
	//                             rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
	//                         }
	//                     }
	//                 }
	//             }
	//         }
	//         return orderSn;
	//     }
	//     return null;
	// }


	private void saveSecKillSession(List<SeckillSessionWithSkusVo> sessions) {
		sessions.stream().forEach(session -> {
			// 存在Redis里的key：seckill:sessions:startTime_endTime，value（list类型）：sessionId-skuId
			String key = SESSION_CACHE_PREFIX + session.getStartTime().getTime() + "_" + session.getEndTime().getTime();
			// 保证幂等性：之前定时任务放入过缓存的就不再放入了
			if (!redisTemplate.hasKey(key)) {
				List<String> values = session.getRelationSkus().stream()
						.map(sku -> sku.getPromotionSessionId() + "-" + sku.getSkuId())
						.collect(Collectors.toList());
				// 缓存活动信息，List结构
				redisTemplate.opsForList().leftPushAll(key, values);
			}
		});
	}

	private void saveSecKillSku(List<SeckillSessionWithSkusVo> sessions) {
		// Hash 结构，缓存秒杀活动里的所有商品SKU信息
		BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
		sessions.stream().forEach(session -> {
			session.getRelationSkus().stream().forEach(sku -> {
				// 每一场秒杀活动里的每一个 skuId
				String key = sku.getPromotionSessionId() + "-" + sku.getSkuId();
				// Redis 里保存的是 场次Id-SkuId，这样能保证不同场次的相同sku也能正常上架
				// 保证幂等性：之前定时任务放入过缓存的场次 id-skuId 信息就不再放入了
				if (!ops.hasKey(key)) {
					// 1. 保存 seckillSkuVo 信息
					SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
					BeanUtils.copyProperties(sku, redisTo);
					// 2. 保存该商品所属秒杀活动的开始/结束时间
					redisTo.setStartTime(session.getStartTime().getTime());
					redisTo.setEndTime(session.getEndTime().getTime());
					// 3. 根据 skuId 去商品服务远程查询该商品的详细 SKU 信息并保存
					R r = productFeignService.info(sku.getSkuId());

					if (r.getCode() == 0) {
						String skuInfoJson = JSONObject.toJSONString(r.get("skuInfo"));
						SkuInfoVo skuInfo = JSON.parseObject(skuInfoJson, new TypeReference<SkuInfoVo>() {
						});
						// 将该商品的详细 SKU 信息保存到 redis 里
						redisTo.setSkuInfoVo(skuInfo);
					} else {
						log.error("无法查询到skuInfo");
					}
					// 4. 生成商品随机码，防止恶意攻击（脚本刷单）
					String token = UUID.randomUUID().toString().replace("-", "");
					redisTo.setRandomCode(token);

					// 5. 序列化为 JSON 并保存
					String jsonString = JSON.toJSONString(redisTo);
					ops.put(key, jsonString);

					// 商品信息和库存信息是一起放入的，如果商品信息已经保证了幂等性，则库存信息也一定是幂等性的
					// 所以放到了这个 if 内部
					// 引入分布式信号量。作用：限流，即只允许少数的线程访问，其余都阻塞
					// 6. 使用库存数作为 Redisson 信号量限制库存
					RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
					// 商品可以秒杀的数量作为信号量
					semaphore.trySetPermits(sku.getSeckillCount());
				}
			});
		});
	}
}
