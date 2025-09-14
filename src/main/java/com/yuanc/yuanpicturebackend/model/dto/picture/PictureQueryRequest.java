package com.yuanc.yuanpicturebackend.model.dto.picture;

import com.yuanc.yuanpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)  // 使用Lombok注解，生成equals和hashCode方法，并包含父类的字段
@Data  // 使用Lombok注解，自动生成getter、setter、toString等方法
public class PictureQueryRequest extends PageRequest implements Serializable {  // 图片查询请求类，继承自分页请求类，实现序列化接口

    /**
     * 图片id  // 多行注释，说明图片id字段
     */
    private Long id;  // 图片ID，Long类型

    // 图片名称  // 单行注释，说明图片名称字段
    private String name;  // 图片名称，String类型

    // 图片介绍  // 单行注释，说明图片介绍字段
    private String introduction;  // 图片介绍，String类型

    // 图片分类  // 单行注释，说明图片分类字段
    private String category;  // 图片分类，String类型

    // 图片标签列表  // 单行注释，说明图片标签列表字段
    private List<String> tags;  // 图片标签列表，String类型的List

    private Long picSize;  // 图片大小，Long类型

    private Integer picWidth;  // 图片宽度，Integer类型

    private Integer picHeight;  // 图片高度，Integer类型

    private Double picScale;  // 图片比例，Double类型

    private String picFormat;  // 图片格式，String类型

    private String searchText;  // 搜索文本，String类型

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;


    private Long spaceId ; // 空间ID，Long类型

    private boolean nullSpaceId;  // 是否为空空间ID，boolean类型

    private Long userId;  // 用户ID，Long类型


    // 序列化版本号，用于Java序列化  // 单行注释，说明serialVersionUID字段
    private static final long serialVersionUID = -3573059265143237935L;  // 序列化版本UID，用于Java序列化时的版本控制
}
