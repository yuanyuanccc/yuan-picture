package com.yuanc.yuanpicturebackend.common;

import com.yuanc.yuanpicturebackend.exception.ErrorCode;

/**
 * 响应结果工具类
 */
public class ResultUtils {

    /**
     * 成功
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code,null,message);
    }
    public static BaseResponse<?> error(ErrorCode errorCode,String message) {
        return new BaseResponse<>(errorCode.getCode(),null,message);
    }
}
