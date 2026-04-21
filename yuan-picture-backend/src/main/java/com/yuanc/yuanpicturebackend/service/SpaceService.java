package com.yuanc.yuanpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuanc.yuanpicturebackend.model.dto.space.SpaceAddRequest;
import com.yuanc.yuanpicturebackend.model.dto.space.SpaceQueryRequest;
import com.yuanc.yuanpicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanc.yuanpicturebackend.model.entity.User;
import com.yuanc.yuanpicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Yuanc
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2026-04-19 23:24:55
*/
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间
     *
     * @param space
     * @param add   是否为创建时检验
     */
    void validSpace(Space space, boolean add);

    /**
     * 获取空间包装类（单条）
     *
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间包装类（分页）
     *
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取查询对象
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间级别填充空间对象
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);


    /**
     * 校验空间权限
     *
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);
}
