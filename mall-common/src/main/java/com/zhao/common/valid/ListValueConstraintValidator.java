package com.zhao.common.valid;

import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yuyun zhao
 * @date 2021/12/28 14:18
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {
	// 存储合法的值
	private Set<Integer> set = new HashSet<>();

	/**
	 * 初始化方法。初始化时，将注解中标注的值加入到 set 中，后续就可以从该 set 中判断前端传来的数据是否在该范围内
	 */
	@Override
	public void initialize(ListValue constraintAnnotation) {
		int[] vals = constraintAnnotation.vals();
		for(int val : vals) {
			// 将结果添加到set集合
			set.add(val);
		}
	}

	/**
	 *	判断效验是否成功
	 * @param value 需要效验的值
	 * @param context 上下文环境
	 * @return 返回是否包含当前值
	 */
	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
		// 判断是包含该值
		return set.contains(value);
	}
}
