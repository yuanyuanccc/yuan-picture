package com.yuanc.yuanpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yuanc.yuanpicturebackend.annotation.AuthCheck;
import com.yuanc.yuanpicturebackend.common.BaseResponse;
import com.yuanc.yuanpicturebackend.common.DeleteRequest;
import com.yuanc.yuanpicturebackend.common.ResultUtils;
import com.yuanc.yuanpicturebackend.constant.UserConstant;
import com.yuanc.yuanpicturebackend.exception.BusinessException;
import com.yuanc.yuanpicturebackend.exception.ErrorCode;
import com.yuanc.yuanpicturebackend.exception.ThrowUtils;
import com.yuanc.yuanpicturebackend.model.dto.picture.*;
import com.yuanc.yuanpicturebackend.model.entity.Picture;
import com.yuanc.yuanpicturebackend.model.entity.Space;
import com.yuanc.yuanpicturebackend.model.entity.User;
import com.yuanc.yuanpicturebackend.model.enums.PictureReviewStatusEnum;
import com.yuanc.yuanpicturebackend.model.vo.PictureTagCategory;
import com.yuanc.yuanpicturebackend.model.vo.PictureVO;
import com.yuanc.yuanpicturebackend.service.PictureService;
import com.yuanc.yuanpicturebackend.service.SpaceService;
import com.yuanc.yuanpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SpaceService spaceService;

    /**
     * 本地缓存
     */
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();


    /**
     * 本地上传图片
     *
     * @param multipartFile        上传的图片文件
     * @param pictureUploadRequest 图片上传请求参数
     * @param request              HTTP请求对象
     * @return 返回上传后的图片信息封装对象
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,  // 接收上传的文件
                                                 PictureUploadRequest pictureUploadRequest,       // 图片上传请求参数
                                                 HttpServletRequest request) {                   // HTTP请求对象
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 调用服务层处理图片上传
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        // 返回成功响应，包含上传后的图片信息
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过 URL 上传图片
     *
     * @param pictureUploadRequest 图片上传请求参数
     * @param request              HTTP请求对象
     * @return 返回上传后的图片信息封装对象
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest,       // 图片上传请求参数
                                                      HttpServletRequest request) {                   // HTTP请求对象
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 调用服务层处理图片上传
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        // 返回成功响应，包含上传后的图片信息
        return ResultUtils.success(pictureVO);
    }


    /**
     * 删除图片的接口方法
     *
     * @param deleteRequest 包含要删除的图片ID的请求对象
     * @param request       HTTP请求对象，用于获取用户信息
     * @return 返回操作结果，成功返回true
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验请求参数是否合法
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 获取要删除的图片ID
        Long id = deleteRequest.getId();
        pictureService.deletePicture(id, loginUser);
        return ResultUtils.success(true);
    }


    /**
     * 更新图片信息的接口
     *
     * @param pictureUpdateRequest 包含要更新的图片信息的请求对象
     * @param request              HTTP请求对象
     * @return 返回操作结果，成功返回true
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 需要管理员权限才能访问此接口
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        // 检查请求参数是否有效
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建新的Picture对象并复制请求中的属性
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);

        // 将标签列表转换为JSON字符串格式存储
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        // 验证图片数据的有效性
        pictureService.validPicture(picture);

        // 获取图片ID并查询数据库中的旧图片信息
        Long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        // 如果找不到对应的图片，则抛出异常
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);

        // 更新图片信息到数据库
        boolean result = pictureService.updateById(picture);
        // 如果更新失败，则抛出异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回操作成功的结果
        return ResultUtils.success(true);
    }


    /**
     * 根据图片ID获取图片信息
     * 需要管理员权限才能访问
     *
     * @param id      图片ID
     * @param request HTTP请求对象
     * @return 返回包含图片信息的BaseResponse对象
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 验证用户是否具有管理员权限
    public BaseResponse<Picture> getPictureById(Long id, HttpServletRequest request) {
        // 检查ID是否为空或小于等于0
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据ID从数据库获取图片信息
        Picture picture = pictureService.getById(id);
        // 如果图片不存在则抛出异常
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 返回成功响应，包含图片信息
        return ResultUtils.success(picture);
    }

    /**
     * 根据ID获取图片视图对象(VO)
     *
     * @param id      图片ID
     * @param request HTTP请求对象，用于获取请求相关信息
     * @return 返回包含PictureVO的BaseResponse对象
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(Long id, HttpServletRequest request) {
        // 检查ID参数是否有效，如果无效则抛出参数错误异常
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据ID从数据库获取图片实体对象
        Picture picture = pictureService.getById(id);
        // 检查图片是否存在，如果不存在则抛出未找到异常
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        //空间权限校验
        Long spaceId = picture.getSpaceId();
        if(spaceId != null){
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser,picture);
        }
        // 调用服务方法获取图片的视图对象并返回成功响应
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();

        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }


    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if(spaceId == null){
            //公开图库
            //普通用户默认只能看到审核通过的图片
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        }else {
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            if(!loginUser.getId().equals(space.getUserId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有空间权限");
            }
        }

        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }


//    //分布式缓存Redis
//    @PostMapping("/list/page/vo/cache")
//    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,HttpServletRequest request){
//        int current = pictureQueryRequest.getCurrent();
//        int size = pictureQueryRequest.getPageSize();
//        ThrowUtils.throwIf(size>20,ErrorCode.PARAMS_ERROR);
//        //普通用户默认只能看到审核通过的图片
//        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
//        //查询缓存
//        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
//        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
//        String redisKey = String.format("yuanpicture:listPictureVOByPage:%s",hashKey);
//        //操作Redis，从缓存中查询
//        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
//        String cachedValue = opsForValue.get(redisKey);
//        if(cachedValue != null){
//            //如果缓存中有结果，缓存结果
//            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
//            return ResultUtils.success(cachedPage);
//        }
//        //查询数据库
//        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
//                pictureService.getQueryWrapper(pictureQueryRequest));
//        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
//        //存入Redis缓存
//        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
//        //设置缓存的过期时间 5-10分钟过期，防止缓存雪崩
//        int cacheExpireTime = 300 + RandomUtil.randomInt(0,300);
//        opsForValue.set(redisKey,cacheValue,cacheExpireTime,TimeUnit.SECONDS);
//        return ResultUtils.success(pictureVOPage);
//    }


    //分布式缓存Redis
    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //普通用户默认只能看到审核通过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        //查询缓存
        //构建缓存key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format("listPictureVOByPage:%s", hashKey);
        //1.先查本地缓存
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            //如果缓存中有结果，返回结果
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        //2.如果本地缓存没有结果，再查Redis缓存
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        cachedValue = opsForValue.get(cacheKey);

        if (cachedValue != null) {
            //如果缓存中有结果，更新本地缓存，返回结果
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        //3.查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        //4.更新缓存
        //更新 Redis 缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        //设置缓存的过期时间 5-10分钟过期，防止缓存雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        opsForValue.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
        //更新本地缓存
        LOCAL_CACHE.put(cacheKey, cacheValue);
        return ResultUtils.success(pictureVOPage);
    }


    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        log.info("编辑图片请求: {}", JSONUtil.toJsonStr(pictureEditRequest));
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * 审核图片
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取图片
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Integer uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }


}
