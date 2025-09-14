package com.yuanc.yuanpicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.yuanc.yuanpicturebackend.model.entity.Space;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SpaceVO implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String spaceName;

    private Integer spaceLevel;

    private Long maxSize;

    private Long maxCount;

    private Long totalCount;

    private Long totalSize;

    private Long userId;

    private Date createTime;

    private Date editTime;

    private Date updateTime;

    private UserVO user;

    private static final long serialVersionUID = 1L;

    /**
     * 封装类转VO
     *
     * @param SpaceVO
     * @return
     */
    public static Space voToObj(SpaceVO SpaceVO) {
        if(SpaceVO == null) {
            return null;
        }
        Space space = new Space();
        BeanUtil.copyProperties(SpaceVO, space);
        return space;
    }


    /**
     * 实体类转封装类
     *
     * @param space@return
     */
    public static SpaceVO objToVo(Space space) {
        if(space == null) {
            return null;
        }
        SpaceVO spaceVO = new SpaceVO();
        BeanUtil.copyProperties(space, spaceVO);
        return spaceVO;
    }
}
