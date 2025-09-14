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
import com.yuanc.yuanpicturebackend.model.dto.space.*;
import com.yuanc.yuanpicturebackend.model.entity.Space;
import com.yuanc.yuanpicturebackend.model.entity.User;
import com.yuanc.yuanpicturebackend.model.enums.SpaceLevelEnum;
import com.yuanc.yuanpicturebackend.model.vo.SpaceVO;
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
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private  UserService userService;

    @Resource
    private  SpaceService spaceService;


    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request){
        ThrowUtils.throwIf(spaceAddRequest == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long result = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(result);
    }




    /**
 * 删除空间的接口方法
 * @param deleteRequest 包含要删除的空间ID的请求对象
 * @param request HTTP请求对象，用于获取用户信息
 * @return 返回操作结果，成功返回true
 */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
    // 校验请求参数是否合法
        if(deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
    // 获取要删除的空间ID
        Long id = deleteRequest.getId();
    // 根据ID查询空间信息
        Space oldSpace = spaceService.getById(id);
    // 如果空间不存在则抛出异常
        ThrowUtils.throwIf(oldSpace == null,ErrorCode.NOT_FOUND_ERROR);
    // 校验用户是否有权限删除该空间（只有空间所有者或管理员可以删除）
        if(!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    // 执行删除操作
        boolean result = spaceService.removeById(id);
    // 如果删除失败则抛出异常
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        //清除空间资源
    // 返回成功结果
        return ResultUtils.success(true);
    }



/**
 * 更新空间信息的接口
 * @param spaceUpdateRequest 包含要更新的空间信息的请求对象
 * @param request HTTP请求对象
 * @return 返回操作结果，成功返回true
 */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 需要管理员权限才能访问此接口
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest,HttpServletRequest request){
    // 检查请求参数是否有效
        if(spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    // 创建新的Space对象并复制请求中的属性
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest,space);
    //自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
    // 验证空间数据的有效性
        spaceService.validSpace(space,false);

    // 获取空间ID并查询数据库中的旧空间信息
        Long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
    // 如果找不到对应的空间，则抛出异常
        ThrowUtils.throwIf(oldSpace == null,ErrorCode.NOT_FOUND_ERROR);

    // 更新空间信息到数据库
        boolean result = spaceService.updateById(space);
    // 如果更新失败，则抛出异常
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
    // 返回操作成功的结果
        return ResultUtils.success(true);
    }


/**
 * 根据空间ID获取空间信息
 * 需要管理员权限才能访问
 * @param id 空间ID
 * @param request HTTP请求对象
 * @return 返回包含空间信息的BaseResponse对象
 */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 验证用户是否具有管理员权限
    public BaseResponse<Space> getSpaceById(Long id, HttpServletRequest request){
    // 检查ID是否为空或小于等于0
        if(id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    // 根据ID从数据库获取空间信息
        Space space = spaceService.getById(id);
    // 如果空间不存在则抛出异常
        ThrowUtils.throwIf(space == null,ErrorCode.NOT_FOUND_ERROR);
    // 返回成功响应，包含空间信息
        return ResultUtils.success(space);
    }

/**
 * 根据ID获取空间视图对象(VO)
 * @param id 空间ID
 * @param request HTTP请求对象，用于获取请求相关信息
 * @return 返回包含SpaceVO的BaseResponse对象
 */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(Long id, HttpServletRequest request){
    // 检查ID参数是否有效，如果无效则抛出参数错误异常
        if(id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    // 根据ID从数据库获取空间实体对象
        Space space = spaceService.getById(id);
    // 检查空间是否存在，如果不存在则抛出未找到异常
        ThrowUtils.throwIf(space == null,ErrorCode.NOT_FOUND_ERROR);
    // 调用服务方法获取空间的视图对象并返回成功响应
        return ResultUtils.success(spaceService.getSpaceVO(space,request));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest){
        int current = spaceQueryRequest.getCurrent();
        int size = spaceQueryRequest.getPageSize();

        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }


    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,HttpServletRequest request){
        int current = spaceQueryRequest.getCurrent();
        int size = spaceQueryRequest.getPageSize();
        ThrowUtils.throwIf(size>20,ErrorCode.PARAMS_ERROR);

        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage,request));
    }



    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        log.info("编辑空间请求: {}", JSONUtil.toJsonStr(spaceEditRequest));
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        spaceService.fillSpaceBySpaceLevel(space);
        // 设置编辑时间
        space.setEditTime(new Date());
        // 数据校验
        spaceService.validSpace(space,false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间级别列表，便于前端展示
     * @return
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel(){
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> {
                    return new SpaceLevel(
                            spaceLevelEnum.getValue(),
                            spaceLevelEnum.getText(),
                            spaceLevelEnum.getMaxCount(),
                            spaceLevelEnum.getMaxSize()
                    );
                }).collect(Collectors.toList());

        return ResultUtils.success(spaceLevelList);
    }

}
