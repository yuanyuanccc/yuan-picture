package com.yuanc.yuanpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 批量导入图片请求
 * 该类用于封装批量上传图片时的请求参数，实现了Serializable接口以支持序列化
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {


    /**
     * 搜索文本，用于筛选需要导入的图片
     */
    private String searchText;


    /**
     * 导入图片的数量，默认值为10
     */
    private Integer count = 10;

    /**
     * 图片名称前缀，为上传的图片添加统一的前缀
     */
    private String namePrefix;



    // 序列化版本号，用于Java序列化
    private static final long serialVersionUID = -3573059265143237935L;
}
