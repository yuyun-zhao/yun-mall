package com.zhao.yunmall.product.exception;

import com.zhao.common.exception.BizCodeEnum;
import com.zhao.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理所有异常，AOP的思想，拦截Controller层抛出的所有异常，无侵入的实现统一异常处理
 * @author yuyun zhao
 * @date 2021/12/28 10:47
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.zhao.yunmall.product.controller")
// 效果等同于 @ResponseBody + @ControllerAdvice
public class MallExceptionControllerAdvice {

	/**
	 * 统一处理异常
	 * @param e：Controller层抛出的MethodArgumentNotValidException异常会被当前方法捕获，处理完毕后发送回前端
	 * @return R.error()
	 */
	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public R handleValidException(MethodArgumentNotValidException e) {
		log.error("数据校验出现问题{}，异常类型{}", e.getMessage(), e.getClass());
		BindingResult bindingResult = e.getBindingResult();
		// 将异常的字段以及其默认展示的信息封装到map中
		Map<String, String> map = new HashMap<>();
		bindingResult.getFieldErrors().forEach(item -> map.put(item.getField(), item.getDefaultMessage()));
		// 以JSON形式返回
		return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", map);
	}

	/**
	 * 拦截Controller层抛出的其他异常（优先级最低，用于兜底拦截）
	 * @param throwable：拦截的异常
	 * @return R.error()
	 */
	@ExceptionHandler(value = Throwable.class)
	public R handlException(Throwable throwable) {
		log.error("错误", throwable);
		return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
	}


}
