package com.yuanc.yuanpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditRequest implements Serializable {

    /**
     * 图片id
     * 用于唯一标识一张图片
     */
    private Long id;

    // 图片名称
    // 用于存储图片的显示名称
    private String name;

    // 图片介绍
    // 用于存储图片的详细描述信息
    private String introduction;

    // 图片分类
    // 用于对图片进行分类管理
    private String category;

    // 图片标签列表
    // 用于存储图片的相关标签，便于检索和分类
    private List<String> tags;


    // 序列化版本号，用于Java序列化
    // 当类结构发生变化时，用于确保序列化和反序列化的兼容性
    private static final long serialVersionUID = -3573059265143237935L;
}
