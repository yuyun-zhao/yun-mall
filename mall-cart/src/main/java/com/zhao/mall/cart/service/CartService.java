package com.zhao.mall.cart.service;

import com.zhao.mall.cart.vo.CartItemVo;
import com.zhao.mall.cart.vo.CartVo;

import java.util.List;

/**
 * @author yuyun zhao
 * @date 2022/1/14 16:15
 */
public interface CartService {
	CartItemVo addCartItem(Long skuId, Integer num);

	CartItemVo getCartItem(Long skuId);

	CartVo getCart();

	void checkCart(Long skuId, Integer isChecked);

	void changeItemCount(Long skuId, Integer num);

	void deleteItem(Long skuId);

	List<CartItemVo> getCheckedItems();
}
