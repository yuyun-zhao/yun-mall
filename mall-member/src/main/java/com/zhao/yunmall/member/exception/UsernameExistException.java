package com.zhao.yunmall.member.exception;

/**
 * @author yuyun zhao
 * @date 2022/1/12 17:13
 */
public class UsernameExistException extends RuntimeException{

	public UsernameExistException() {
		super("用户名已存在");
	}
}
