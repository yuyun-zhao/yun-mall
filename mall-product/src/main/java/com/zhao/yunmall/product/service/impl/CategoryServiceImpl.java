package com.zhao.yunmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zhao.yunmall.product.service.CategoryBrandRelationService;
import com.zhao.yunmall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.product.dao.CategoryDao;
import com.zhao.yunmall.product.entity.CategoryEntity;
import com.zhao.yunmall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

	@Autowired
	CategoryBrandRelationService relationService;

	@Autowired
	StringRedisTemplate redisTemplate;

	@Autowired
	RedissonClient redissonClient;

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		IPage<CategoryEntity> page = this.page(
				new Query<CategoryEntity>().getPage(params),
				new QueryWrapper<CategoryEntity>()
		);

		return new PageUtils(page);
	}

	@Override
	public List<CategoryEntity> listWithTree() {
		// 1. 查出所有商品分类
		List<CategoryEntity> entities = baseMapper.selectList(null);

		// 2. 组装成父子的树形结构
		// 2.1 找到所有的一级分类商品（根据parentCid字段父种类id为0的筛选出来）
		// 2.2 递归地设置每种商品的子商品
		// 2.3 按照每种商品的sort字段进行排序
		List<CategoryEntity> menusLevel1 = entities.stream()
				.filter(categoryEntity -> categoryEntity.getParentCid() == 0)
				.map((menu) -> {
					// 当前管道输入的menu是已经经过过滤的一级商品，设置其孩子为parentCid等于自己的商品
					// 同时该方法内也将递归地设置其孩子商品的孩子商品，从而完成所有商品的分类
					menu.setChildren(getChildren(menu, entities));
					// 当前的menu是父菜单
					return menu;
				})
				.sorted((m1, m2) -> (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort()))
				.collect(Collectors.toList());

		return menusLevel1;
	}

	@Override
	public void removeMenusByIds(List<Long> idList) {
		// TODO：1. 检查当前要删除的菜单是否被其他菜单所引用
		// 不使用物理删除，而是使用逻辑删除

		baseMapper.deleteBatchIds(idList);

	}

	/**
	 * 查询当前属性所属商品的分类路径（从一级到三级）
	 *
	 * @param catelogId 当前属性所属商品id
	 * @return 其分类路径
	 */
	@Override
	public Long[] findCatelogPath(Long catelogId) {
		List<Long> paths = new ArrayList<>();
		// 递归添加当前商品的父商品id
		findParentPathRecur(catelogId, paths);
		return paths.toArray(new Long[0]);
	}

	/**
	 * 级联更新所有关联的数据：先更新商品表，然后更新关联表
	 * 因为涉及到两个表的更新，因此需要添加事务注解，开启事务
	 * @CacheEvict 失效模式：当修改了商品数据后，就将其缓存数据清空。下次再查询时再将其加入到缓存中
	 * @Caching 进行组合，可以同时进行多种缓存操作
	 * @CacheEvict(value = {"category", "product"}, allEntries = true) 同时删掉该分区下的所有缓存。该分区并不在Redis中，只是在Spring CacheManger内进行分区的
	 *
	 * 约定：存储同一类型的数据都存储在同一个分区下。分区名默认就是缓存的前缀，效果 category:value，
	 * 例如下面这个例子存储的key就是：category:getCatalogJson。这样在 Redis 的可视化界面里，存储的效果就类似于分组了
	 * 前提是不指定prefix前缀，就会这个效果
	 * 这时截个图
	 *
	 * @CachePut // 双写模式可以使用该接口。当更新完数据后，再立即put更新一次 ，要求方法必须有返回值，因为要拿着这个返回值再去写到缓存中
	 * @CacheEvict 失效模式，方法不需要有返回值
	 * SpEl 表达式里，如果是直接取值，就加 'xxx'
	 */
	// @CacheEvict(value = {"category", "product"}, key = "'getCategoryLevel1'") 只能写单个key
	// @CachePut // 双写模式可以使用该接口。当更新完数据后，再立即put更新一次
	@Caching(evict = {
			@CacheEvict(value = {"category", "product"}, key = "'getCategoryLevel1'"),
			@CacheEvict(value = {"category", "product"}, key = "'getCatalogJson'")
	})
	@Transactional
	@Override
	public void updateCascade(CategoryEntity category) {
		// 先更新商品表
		this.updateById(category);
		// 更新关联表中的数据
		relationService.updateCategoryName(category.getCatId(), category.getName());
	}

	/**
	 * 获取一级分类
	 * @Cacheable 代表当前方法的结果需要缓存。如果缓存中有，方法就不会被调用，如果缓存中没有就会调用方法再将调用结果放到缓存中
	 * 原理应该也是AOP，如果缓存有直接不执行该方法的内容了
	 * 每一个需要缓存的数据，都要指定该数据要放到哪个名字的缓存中【缓存的区分（按照业务类型分区）】
	 *
	 * @return
	 */
	@Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
	@Override
	public List<CategoryEntity> getCategoryLevel1() {
		System.out.println("方法执行了");
		return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
	}

	/**
	 * 使用注解自动添加缓存
	 * @return
	 */
	@Cacheable(value = {"category", "product"}, key = "#root.method.name")
	@Override
	public Map<String, List<Catalog2Vo>> getCatalogJson() {
		// 不需要再手动写缓存的代码了
		return getCatalogJsonFromDB();
	}

	/**
	 * 先查缓存，如果缓存没命中再去数据库查
	 * TODO 可能产生堆外内存溢出问题 OutOfDirectMemoryError
	 *  1. SpringBoot 2.0 默认使用 lettuce 作为 Redis 客户端，它使用 netty 进行网络通信
	 *  2. 因为 lettuce 的 bug 导致 netty 堆外内存溢出
	 *  3. netty 如果没有指定堆外内存大小，则默认使用 -Xmx 里设置的大小。因为内存没有及时释放
	 *  4. 可以通过 -Dio.netty.maxDirectMemory 设置堆外内存
	 *  解决方案：不能仅通过增大 -Dio.netty.maxDirectMemory 来解决，因为内存总会占满
	 *  1. 升级 lettuce 客户端 （新版没有）
	 *  2. 使用 jedis
	 *
	 * lettuce 和 jedis 都是操作 redis 的底层客户端
	 * Spring 在二者的基础上进行了二次封装，就得到了 reidstemplate
	 * @return
	 */
	/**
	 * 先查询缓存是否存在，如果存在就直接返回，否则先加上分布式锁，然后再去数据库里查
	 */
	public Map<String, List<Catalog2Vo>> getCatalogJsonWithRedis() {
		/**
		 * 1. 空结果缓存（或布隆过滤器）：解决缓存穿透
		 * 2. 设置过期时间（加随机值）：解决缓存雪崩
		 * 3. 加锁：解决缓存击穿
		 */
		// 序列化：先将Java对象转成JSON字符串，然后向缓存中存储JSON字符串，
		// 反序列化：读取时也是读取出JSON字符串，再转成Java对象使用

		// 1. 先查询是否有缓存
		String cache = redisTemplate.opsForValue().get("catalogJSON");
		if (StringUtils.isEmpty(cache)) {
			// 2. 缓存如果没命中，先加上分布式锁，然后再去数据库里查数据
			Map<String, List<Catalog2Vo>> catalogJson = getCatalogJsonFromDBWithRedissonLock();
			// 将Java对象转换成JSON字符串
			String s = JSON.toJSONString(catalogJson);
			// 3. 将查询到的数据放入缓存
			redisTemplate.opsForValue().set("catalogJSON", s);
			return catalogJson;
		}

		// 将缓存中的JSON字符串转换成实际对象。其中，TypeReference 以匿名内部类的形式创建
		Map<String, List<Catalog2Vo>> result = JSON.parseObject(cache, new TypeReference<Map<String, List<Catalog2Vo>>>() {
		});
		return result;
	}

	/**
	 * 如果Redis中缓存不存在，先加上分布式锁，然后再查数据库
	 * 使用Redisson操作分布式锁
	 * @return
	 */
	public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
		// 1. 原子性加锁，其内会自动设置过期时间（看门狗+自动续期机制）。也可以手动指定过期时间
		RLock lock = redissonClient.getLock("catalogJson-lock");
		lock.lock();

		Map<String, List<Catalog2Vo>> catalogJsonFromDB;
		try {
			// 2. 加锁后，去数据库里查数据
			catalogJsonFromDB = getCatalogJsonFromDB();
		} finally {
			// 3. 最后原子性解锁
			lock.unlock();
		}
		return catalogJsonFromDB;
	}


	/**
	 * 从数据库中查数据并进行封装
	 * @return
	 */
	public Map<String, List<Catalog2Vo>> getCatalogJsonFromDB() {
		// 1. 查出二级分类
		List<CategoryEntity> categoryEntities = this.list(new QueryWrapper<CategoryEntity>().eq("cat_level", 2));

		// 2. 获取每个二级分类的 Catalog2Vo 包装对象
		List<Catalog2Vo> catalog2Vos = categoryEntities.stream().map(categoryEntity -> {
			// 查出每个二级分类查下的三级分类
			List<CategoryEntity> level3 = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", categoryEntity.getCatId()));
			// 将查到的三级分类包装成一个 Catalog3Vo 对象
			List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3.stream().map(cat -> {
				return new Catalog2Vo.Catalog3Vo(cat.getParentCid().toString(), cat.getCatId().toString(), cat.getName());
			}).collect(Collectors.toList());
			// 为每个二级分类包装出对应的 Catalog2Vo 对象
			Catalog2Vo catalog2Vo = new Catalog2Vo(categoryEntity.getParentCid().toString(), categoryEntity.getCatId().toString(), categoryEntity.getName(), catalog3Vos);
			return catalog2Vo;
		}).collect(Collectors.toList());

		// 3. 包装成map
		Map<String, List<Catalog2Vo>> catalogMap = new HashMap<>();
		for (Catalog2Vo catalog2Vo : catalog2Vos) {
			// 查出当前分类的父节点list
			List<Catalog2Vo> list = catalogMap.getOrDefault(catalog2Vo.getCatalog1Id(), new LinkedList<>());
			// 将当前节点插入到其父节点的list中
			list.add(catalog2Vo);
			// 保存一级节点对应的list到map，将返回该map给前端进行渲染三级分类
			catalogMap.put(catalog2Vo.getCatalog1Id(), list);
		}
		return catalogMap;
	}

	/**
	 * 递归添加当前商品的父商品id
	 *
	 * @param catelogId 当前商品id
	 * @param paths     添加到结果集合中
	 */
	public void findParentPathRecur(Long catelogId, List<Long> paths) {
		CategoryEntity currCategoryEntity = this.getById(catelogId);
		if (currCategoryEntity.getParentCid() != 0) {
			findParentPathRecur(currCategoryEntity.getParentCid(), paths);
		}
		paths.add(catelogId);
	}

	/**
	 * 递归设置所有菜单的子菜单
	 *
	 * @param root：当前商品
	 * @param all：所有商品
	 * @return：当前商品的直接孩子商品
	 */
	public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
		// 获取当前商品类型root的子类型children，并且在其内递归的设置children的子类型
		List<CategoryEntity> children = all.stream()
				.filter(entity -> entity.getParentCid().equals(root.getCatId()))
				.map(menu -> {
					// 为当前的商品类型递归地设置其子类型
					menu.setChildren(getChildren(menu, all));
					return menu;
				}).sorted((m1, m2) -> (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort()))
				.collect(Collectors.toList());
		return children;
	}


}