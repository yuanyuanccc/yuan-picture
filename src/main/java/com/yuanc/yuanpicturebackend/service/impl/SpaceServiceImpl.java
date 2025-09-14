package com.yuanc.yuanpicturebackend.service.impl;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanc.yuanpicturebackend.exception.BusinessException;
import com.yuanc.yuanpicturebackend.exception.ErrorCode;
import com.yuanc.yuanpicturebackend.exception.ThrowUtils;
import com.yuanc.yuanpicturebackend.model.dto.space.SpaceAddRequest;
import com.yuanc.yuanpicturebackend.model.dto.space.SpaceQueryRequest;
import com.yuanc.yuanpicturebackend.model.entity.Picture;
import com.yuanc.yuanpicturebackend.model.entity.Space;
import com.yuanc.yuanpicturebackend.model.entity.User;
import com.yuanc.yuanpicturebackend.model.enums.SpaceLevelEnum;
import com.yuanc.yuanpicturebackend.model.vo.PictureVO;
import com.yuanc.yuanpicturebackend.model.vo.SpaceVO;
import com.yuanc.yuanpicturebackend.model.vo.UserVO;
import com.yuanc.yuanpicturebackend.service.SpaceService;
import com.yuanc.yuanpicturebackend.mapper.SpaceMapper;
import com.yuanc.yuanpicturebackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
* @author Yuanc
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-09-14 07:31:03
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //1.填充参数默认值
        //转换实体类和DTO
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        if(StrUtil.isBlank(space.getSpaceName())){
            space.setSpaceName("默认空间");
        }
        if(ObjUtil.isEmpty(space.getSpaceLevel())){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        //填充容量和大小
        this.fillSpaceBySpaceLevel(space);
        //2.校验参数、
        this.validSpace(space, true);
        //3.校验权限
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if(SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限创建指定级别的空间");
        }
        //4，控制同一用户只能创建一个私有空间
        String lock = String.valueOf(userId).intern();
        synchronized(lock){
            Long newSpaceId = transactionTemplate.execute(status -> {
                //判断是否已有空间
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "同一用户只能创建一个私有空间");
                //创建
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建失败");
                return space.getId();
            });
            return newSpaceId;
        }
    }

    @Override
    public void validSpace(Space space , boolean add) {
        // 检查picture对象是否为null，为null则抛出参数错误异常
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        //创建时校验
        if(add){
            // 检查spaceName是否为空，为空则抛出参数错误异常，并提示"空间名不能为空"
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名不能为空");
            // 检查spaceLevel是否为空，为空则抛出参数错误异常，并提示"空间等级不能为空"
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间等级不能为空");
        }
        // 修改数据时，空间名称进行校验
        if(StrUtil.isNotBlank(spaceName) && spaceName.length() > 30){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        //修改数据时，空间级别进行校验
        if(spaceLevel != null && spaceLevelEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 将Picture对象转换为PictureVO对象
        SpaceVO spaceVO = SpaceVO.objToVo(space);

        // 获取图片的用户ID
        Long userId = space.getUserId();
        // 如果用户ID存在且大于0，则获取用户信息
        if (userId != null && userId > 0) {
            // 根据用户ID获取用户信息
            User user = userService.getById(userId);
            // 将用户对象转换为UserVO对象
            UserVO userVO = userService.getUserVO(user);
            // 将用户信息设置到PictureVO对象中
            spaceVO.setUser(userVO);
        }
        // 返回填充了用户信息的PictureVO对象
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }

        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());

        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;

    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        // 创建一个新的查询包装器实例
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        // 如果查询请求为空，直接返回空的查询包装器
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从查询请求中获取各个字段的值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        // 添加各种查询条件，使用条件判断确保只在条件有效时添加
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);

        // 设置排序条件，根据指定的字段和排序方式进行排序
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), StrUtil.equals(sortOrder, "ascend"), sortField);
        // 返回构建完成的查询包装器
        return queryWrapper;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if(spaceLevelEnum != null){
            long maxSize = spaceLevelEnum.getMaxSize();
            if(space.getMaxSize() == null)
            {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if(space.getMaxCount() == null){
                space.setMaxCount(maxCount);
            }
        }
    }


}




