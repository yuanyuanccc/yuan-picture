package com.yuanc.yuanpicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 图片审核状态枚举类
 */
@Getter
public enum PictureReviewStatusEnum {

    REVIWING("待审核",0),
    PASS("通过",1),
    REJECT("拒绝",2);


    private final String text;

    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if(ObjectUtil.isEmpty(value)){
            return null;
        }
        for (PictureReviewStatusEnum PictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if(PictureReviewStatusEnum.value == value){
                return PictureReviewStatusEnum;
            }
        }
        return null;
    }

}
