package com.yuanc.yuanpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新用户请求
 */
@Data
public class UserUpdateRequest implements Serializable {


    private static final long serialVersionUID = 8687280907651823778L;

    private Long id;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;

}
