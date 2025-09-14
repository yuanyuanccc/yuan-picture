package com.yuanc.yuanpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureUpdateRequest implements Serializable {

    /**
     * 图片id
     */
    private Long id;

    // 图片名称
    private String name;

    // 图片介绍
    private String introduction;

    // 图片分类
    private String category;

    // 图片标签列表
    private List<String> tags;


    // 序列化版本号，用于Java序列化
    private static final long serialVersionUID = -3573059265143237935L;
}
