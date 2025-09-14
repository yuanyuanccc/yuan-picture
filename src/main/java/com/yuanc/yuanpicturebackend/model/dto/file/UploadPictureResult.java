package com.yuanc.yuanpicturebackend.model.dto.file;

import lombok.Data;

/**
 * 上传图片结果类
 * 用于封装图片上传后的相关信息
 */
@Data
public class UploadPictureResult {

    private String url;        // 图片访问URL地址

    private String thumbnailUrl;


    private String picName;    // 图片名称

    private Long picSize;    // 图片大小

    private int picWidth;   // 图片宽度

    private int picHeight;  // 图片高度

    private Double picScale;   // 图片比例

    private String picFormat;  // 图片格式
}
