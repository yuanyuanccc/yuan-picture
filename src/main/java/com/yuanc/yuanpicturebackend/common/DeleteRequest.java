package com.yuanc.yuanpicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求
 */
@Data
public class DeleteRequest implements Serializable {

    private Long id;

    private static final long serialVersionUID = 1L;
}
