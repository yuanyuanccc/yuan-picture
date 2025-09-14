package com.yuanc.yuanpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片审核请求
 */
@Data
public class PictureReviewRequest implements Serializable {

    /**
     * id
     */
    private Long id;
    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;



    // 序列化版本号，用于Java序列化
    private static final long serialVersionUID = -3573059265143237935L;
}
