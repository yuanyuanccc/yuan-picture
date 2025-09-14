package com.yuanc.yuanpicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.yuanc.yuanpicturebackend.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureVO implements Serializable {

    private Long id;

    private String url;

    private String thumbnailUrl;

    private String name;

    private String introduction;

    private List<String> tags;

    private String category;

    private Long picSize;

    private Integer picWidth;

    private Integer picHeight;

    private Double picScale;

    private String picFormat;

    private Long userId;

    private Long spaceId;

    private Date createTime;

    private Date editTime;

    private Date updateTime;

    private UserVO user;

    private static final long serialVersionUID = 1L;

    /**
     * 封装类转VO
     * @param pictureVO
     * @return
     */
    public static Picture voToObj(PictureVO pictureVO) {
        if(pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVO, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }


    /**
     * 实体类转封装类
     * @param picture
     * @return
     */
    public static PictureVO objToVo(Picture picture) {
        if(picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(picture, pictureVO);
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }
}
