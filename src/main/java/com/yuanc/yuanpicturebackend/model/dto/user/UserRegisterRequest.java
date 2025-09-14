package com.yuanc.yuanpicturebackend.model.dto.user;


import lombok.Data;

import java.io.Serializable;

/**
 * 用户请求接口
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -8261470332271953514L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
