package com.yuanc.yuanpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuanc.yuanpicturebackend.model.dto.picture.*;
import com.yuanc.yuanpicturebackend.model.dto.user.UserQueryRequest;
import com.yuanc.yuanpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanc.yuanpicturebackend.model.entity.User;
import com.yuanc.yuanpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author Yuanc
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-09-11 09:06:50
*/
public interface PictureService extends IService<Picture> {




/**
 * 验证图片的有效性和合法性
 * 该方法用于检查传入的Picture对象是否符合要求
 *
 * @param picture 需要验证的图片对象
 *              该对象应包含图片的基本信息和数据
 *              方法将检查其格式、大小、完整性等属性
 */
    void validPicture(Picture picture);

/**
 * 上传图片方法
 *
 * @param inputSource 文件输入源
 * @param pictureUploadRequest 图片上传请求参数，包含上传的相关配置信息
 * @param loginUser 当前登录用户信息，用于关联上传者
 * @return PictureVO 返回图片视图对象，包含上传后的图片信息
 */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);



/**
 * 根据Picture对象和HttpServletRequest请求对象获取PictureVO对象
 *
 * @param picture 图片实体对象，包含图片的基本信息
 * @param request HTTP请求对象，用于获取请求相关的信息
 * @return PictureVO 图片视图对象，用于前端展示图片相关信息
 */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);


/**
 * 获取图片视图对象（PictureVO）的分页信息
 * 该方法将原始图片分页对象转换为视图对象分页，可能包含请求相关的处理
 *
 * @param picturePage 原始图片分页对象，包含分页信息和图片数据
 * @param request HTTP请求对象，可能用于获取请求上下文信息
 * @return 返回包含视图对象（PictureVO）的分页信息，用于前端展示
 */
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

/**
 * 根据图片查询请求参数构建查询条件包装器
 *
 * @param pictureQueryRequest 图片查询请求参数对象，包含查询条件
 * @return 返回一个QueryWrapper对象，用于构建数据库查询条件
 */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 图片审核
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充图片审核参数
     */
    void fillReviewParams(Picture picture, User loginUser);


    /**
     * 批量导入图片
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);


    /**
     * 清除图片文件
     */
    void clearPictureFile(Picture oldPicture);


    void deletePicture(long pictureId, User loginUser);

    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 校验空间图片的权限
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);
}
