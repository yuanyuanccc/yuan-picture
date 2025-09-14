package com.yuanc.yuanpicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间添加请求实体类
 * 实现了Serializable接口，支持序列化操作
 * 使用@Data注解，自动生成getter、setter等方法
 */
@Data
public class SpaceAddRequest implements Serializable {

    // 序列化版本UID，用于版本控制
    private static final long serialVersionUID = 1L;

    // 空间名称属性
    private String spaceName;

    // 空间等级属性
    private Integer spaceLevel;
}
