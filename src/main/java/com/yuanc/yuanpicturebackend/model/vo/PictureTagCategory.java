package com.yuanc.yuanpicturebackend.model.vo;


import lombok.Data;

import java.util.List;

import lombok.Data;
/**
 * 图片标签分类类
 * 用于存储和管理图片的标签和分类信息
 */
@Data
public class PictureTagCategory {


    /**
     * 标签列表
     * 用于存储图片相关的所有标签
     */
    private List<String> tagList;

    /**
     * 分类列表
     * 用于存储图片所属的分类信息
     */
    private List<String> categoryList;
}
