package com.yuanc.yuanpicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间编辑请求实体类
 * 用于封装前端传递的空间编辑相关数据
 * 实现Serializable接口以支持序列化操作
 */
@Data // 使用Lombok注解自动生成getter、setter等方法
public class SpaceEditRequest implements Serializable {


    // 序列化版本号UID，用于控制版本序列化/反序列化的兼容性
    private static final long serialVersionUID = 1L;

    // 空间ID，用于标识唯一的空间
    private Long id;

    // 空间名称，用于显示和标识空间
    private String spaceName;
}
