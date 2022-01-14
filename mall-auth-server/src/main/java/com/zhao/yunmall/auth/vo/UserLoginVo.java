package com.zhao.yunmall.auth.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginVo {

    private String loginAccount;
    private String password;
}
