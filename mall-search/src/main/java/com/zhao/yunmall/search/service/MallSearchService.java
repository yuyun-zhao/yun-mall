package com.zhao.yunmall.search.service;

import com.zhao.yunmall.search.vo.SearchParam;
import com.zhao.yunmall.search.vo.SearchResult;

/**
 * @author yuyun zhao
 * @date 2022/1/9 17:41
 */
public interface MallSearchService {
	/**
	 *
	 * @param searchParam 检索的所有参数
	 * @return 返回检索的结果，里面包含页面需要的所有信息
	 */
	SearchResult getSearchResult(SearchParam searchParam);
}
