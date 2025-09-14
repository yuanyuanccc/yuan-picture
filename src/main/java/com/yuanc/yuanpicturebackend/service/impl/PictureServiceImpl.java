package com.yuanc.yuanpicturebackend.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanc.yuanpicturebackend.common.ResultUtils;
import com.yuanc.yuanpicturebackend.exception.BusinessException;
import com.yuanc.yuanpicturebackend.exception.ErrorCode;
import com.yuanc.yuanpicturebackend.exception.ThrowUtils;
import com.yuanc.yuanpicturebackend.manager.CosManager;
import com.yuanc.yuanpicturebackend.manager.FileManager;
import com.yuanc.yuanpicturebackend.manager.upload.FilePictureUpload;
import com.yuanc.yuanpicturebackend.manager.upload.PictureUploadTemplate;
import com.yuanc.yuanpicturebackend.manager.upload.UrlPictureUpload;
import com.yuanc.yuanpicturebackend.model.dto.file.UploadPictureResult;
import com.yuanc.yuanpicturebackend.model.dto.picture.*;
import com.yuanc.yuanpicturebackend.model.entity.Picture;
import com.yuanc.yuanpicturebackend.model.entity.Space;
import com.yuanc.yuanpicturebackend.model.entity.User;
import com.yuanc.yuanpicturebackend.model.enums.PictureReviewStatusEnum;
import com.yuanc.yuanpicturebackend.model.vo.PictureVO;
import com.yuanc.yuanpicturebackend.model.vo.UserVO;
import com.yuanc.yuanpicturebackend.service.PictureService;
import com.yuanc.yuanpicturebackend.mapper.PictureMapper;
import com.yuanc.yuanpicturebackend.service.SpaceService;
import com.yuanc.yuanpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Yuanc
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-09-11 09:06:49
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManager fileManager;
    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Autowired
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 验证Picture对象的有效性
     *
     * @param picture 需要验证的Picture对象
     */
    public void validPicture(Picture picture) {
        // 检查picture对象是否为null，为null则抛出参数错误异常
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 获取Picture对象的id属性
        Long id = picture.getId();
        // 获取Picture对象的url属性
        String url = picture.getUrl();
        // 获取Picture对象的introduction属性
        String introduction = picture.getIntroduction();

        // 检查id是否为空，为空则抛出参数错误异常，并提示"id不能为空"
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id不能为空");
        // 检查url是否不为空，不为空则检查长度是否超过1024，超过则抛出参数错误异常，并提示"url过长"
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url过长");
        }
        // 检查introduction是否不为空，不为空则检查长度是否超过800，超过则抛出参数错误异常，并提示"简介过长"
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }

    }

    @Override
    /**
     * 上传图片方法
     * @param multipartFile 上传的图片文件
     * @param pictureUploadRequest 图片上传请求参数，包含图片ID等信息
     * @param loginUser 当前登录用户信息
     * @return PictureVO 返回图片的视图对象
     */
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数：检查用户是否登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if(spaceId != null){
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            //校验是否有空间的权限
            if(!loginUser.getId().equals(space.getUserId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
            //校验额度
            if(space.getTotalCount() >= space.getMaxCount()){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"空间条数不足");
            }
            if(space.getTotalSize() >= space.getMaxSize()){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"空间容量不足");
            }
        }
        //判断是新增还是删除
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        //如果是更新 判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            //仅本人和管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅本人和管理员可编辑");
            }
            //校验空间是否一致
            //没传，则复用原有的
            if(spaceId == null){
                if(oldPicture.getSpaceId() != null){
                    spaceId = oldPicture.getSpaceId();
                }
            }else{
                //传了，则判断是否一致
                if(ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
                }
            }
        }
        //上传图片,得到图片信息
        //按照空间划分目录
        String uploadPathPrefix;
        if(spaceId == null){
            //公共图库
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        }else {
            //空间
            uploadPathPrefix = String.format("space/%s", spaceId);
        }

        //根据 inputSource 的类型区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        //构造要入库的图片信息
        Picture picture = new Picture();
        picture.setSpaceId(spaceId);
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        //支持外层传入名称
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        //补充审核参数
        this.fillReviewParams(picture, loginUser);

        //操作数据库
        //如果pictureId不为空，需要补充id和编辑时间
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        //如果是更新，可以清理图片资源
        //this.clearPictureFile(oldPicture);
        //开启事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            //插入数据
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "保存图片失败,数据库操作失败");
            if(finalSpaceId != null){
                //更新空间的使用额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "额度更新失败");
            }
            return Boolean.TRUE;
        });

        return PictureVO.objToVo(picture);
    }



    /**
     * 将Picture对象转换为PictureVO对象，并填充用户信息 (单条)
     *
     * @param picture Picture实体对象
     * @param request HttpServletRequest对象，用于获取请求相关信息
     * @return 返回填充了用户信息的PictureVO对象
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {

        // 将Picture对象转换为PictureVO对象
        PictureVO pictureVO = PictureVO.objToVo(picture);

        // 获取图片的用户ID
        Long userId = picture.getUserId();
        // 如果用户ID存在且大于0，则获取用户信息
        if (userId != null && userId > 0) {
            // 根据用户ID获取用户信息
            User user = userService.getById(userId);
            // 将用户对象转换为UserVO对象
            UserVO userVO = userService.getUserVO(user);
            // 将用户信息设置到PictureVO对象中
            pictureVO.setUser(userVO);
        }
        // 返回填充了用户信息的PictureVO对象
        return pictureVO;
    }


    /**
     * 分页获取图片封装
     *
     * @param picturePage
     * @param request
     * @return
     */
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }

        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());

        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;

    }


    /**
     * 根据查询请求条件构建查询包装器
     *
     * @param pictureQueryRequest 图片查询请求对象，包含各种查询条件
     * @return QueryWrapper<Picture> 构建好的查询包装器，用于数据库查询
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        // 创建一个新的查询包装器实例
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 如果查询请求为空，直接返回空的查询包装器
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从查询请求中获取各种查询条件
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        // 处理搜索文本条件，在名称或介绍中模糊匹配搜索文本
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }


        // 添加各种查询条件，使用条件判断确保只在条件有效时添加
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

        // 处理标签条件，对每个标签进行模糊匹配
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }

        // 设置排序条件，根据指定的字段和排序方式进行排序
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), StrUtil.equals(sortOrder, "ascend"), sortField);
        // 返回构建完成的查询包装器
        return queryWrapper;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //1.校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //校验参数是否存在
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }
        //数据库
        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            //管理员自动过审核
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        } else {
            //非管理员
            picture.setReviewStatus(PictureReviewStatusEnum.REVIWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        //校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "一次最多导入30张图片");
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        //名称前缀默认等于搜索关键字
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        //抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        //解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        //遍历元素，依次上传内容
        for (Element imgelement : imgElementList) {
            String fileUrl = imgelement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过：{}", fileUrl);
                continue;
            }
            //处理图片的地址，防止转义和对象存储冲突的问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));

            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("上传图片成功：id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        //判断该图片是否被多人使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();
        if (count > 1) {
            return;
        }
        //删除图片
        cosManager.deleteObject(pictureUrl);
        //删除缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    @Override
    public void deletePicture(long pictureId, User loginUser){
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NO_AUTH_ERROR);
        // 根据ID查询图片信息
        Picture oldPicture = this.getById(pictureId);
        // 如果图片不存在则抛出异常
        ThrowUtils.throwIf(oldPicture == null,ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        this.checkPictureAuth(loginUser,oldPicture);
        //开启事务
        transactionTemplate.execute(status -> {
            //删除
            boolean result = this.removeById(pictureId);
            // 如果删除失败则抛出异常
            ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
            //更新空间的使用额度
            boolean update = spaceService.lambdaUpdate()
                    .eq(Space::getId, oldPicture.getSpaceId())
                    .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                    .setSql("totalCount = totalCount - 1")
                    .update();
            ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "额度更新失败");
            return Boolean.TRUE;
        });
        //清除图片资源
        this.clearPictureFile(oldPicture);
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        //补充审核参数
        this.fillReviewParams(picture,loginUser);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        this.checkPictureAuth(loginUser, oldPicture);
        // 操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        Long loginUserId = loginUser.getId();
        if(spaceId == null)
        {
            //公共图库
            if(!picture.getUserId().equals(loginUserId) && !userService.isAdmin(loginUser))
            {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
            }
        }else{
            //私人图库
            if (!picture.getUserId().equals(loginUserId)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
            }
        }
    }
}




