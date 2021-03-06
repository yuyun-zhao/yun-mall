package com.zhao.yunmall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author yuyun zhao
 * @date 2022/1/12 16:58
 */
@Data
public class MemberRegisterVo implements Serializable {
	private String userName;

	private String password;

	private String phone;
}
