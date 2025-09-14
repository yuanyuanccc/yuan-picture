package com.yuanc.yuanpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 617987677271868385L;

   private String userAccount;

   private String userPassword;

}
