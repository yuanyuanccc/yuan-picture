package com.yuanc.yuanpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 */
@Data
public class UserAddRequest implements Serializable {


    private static final long serialVersionUID = 8687280907651823778L;

    private String userName;

    private String userAccount;

    private String userAvatar;

    private String userProfile;

    private String userRole;

}
