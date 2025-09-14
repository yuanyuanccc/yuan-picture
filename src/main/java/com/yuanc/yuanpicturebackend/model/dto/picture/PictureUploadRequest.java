package com.yuanc.yuanpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片id
     */
    private Long id;  // 图片ID，用于标识唯一的一张图片

    private String fileUrl;  // 图片的文件URL地址，用于访问图片资源

    private String picName;  // 图片的名称，用于展示或标识图片

    private Long spaceId;


    // 序列化版本UID，用于序列化和反序列化时的版本控制
    private static final long serialVersionUID = -3573059265143237935L;
}
