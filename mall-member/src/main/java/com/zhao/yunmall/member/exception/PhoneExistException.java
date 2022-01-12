package com.zhao.yunmall.member.exception;

/**
 * @author yuyun zhao
 * @date 2022/1/12 17:13
 */
public class PhoneExistException extends RuntimeException{
	public PhoneExistException() {
		super("手机号已存在");
	}
}
