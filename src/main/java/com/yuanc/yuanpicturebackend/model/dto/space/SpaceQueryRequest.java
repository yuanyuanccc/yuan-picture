package com.yuanc.yuanpicturebackend.model.dto.space;

import com.yuanc.yuanpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 空间查询请求类，继承自分页请求类，实现了序列化接口
 * 使用了Lombok注解自动生成equals、hashCode、getter和setter方法
 */
@EqualsAndHashCode(callSuper = true) // 确保在生成equals和hashCode方法时包含父类的字段
@Data // Lombok注解，自动生成getter、setter、toString、equals和hashCode方法
public class SpaceQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L; // 序列化版本UID，用于序列化和反序列化时的版本控制

    private Long id; // 空间ID

    private Long userId; // 用户ID

    private String spaceName; // 空间名称

    private Integer spaceLevel; // 空间等级


}
