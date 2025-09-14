package com.yuanc.yuanpicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间更新请求类
 * 用于封装空间更新请求的数据，实现了Serializable接口以支持序列化
 */
@Data
public class SpaceUpdateRequest implements Serializable {


    // 序列化版本UID，用于版本控制
    private static final long serialVersionUID = 1L;

    // 空间ID
    private Long id;

    // 空间名称
    private String spaceName;

    // 空间等级
    private Integer spaceLevel;

    // 空间最大大小
    private Long maxSize;

    // 空间最大数量
    private Long maxCount;
}
