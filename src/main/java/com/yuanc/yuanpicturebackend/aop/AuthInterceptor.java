package com.yuanc.yuanpicturebackend.aop;

import com.yuanc.yuanpicturebackend.annotation.AuthCheck;
import com.yuanc.yuanpicturebackend.exception.BusinessException;
import com.yuanc.yuanpicturebackend.exception.ErrorCode;
import com.yuanc.yuanpicturebackend.model.entity.User;
import com.yuanc.yuanpicturebackend.model.enums.UserRoleEnum;
import com.yuanc.yuanpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //如果不需要权限,放行
        if(mustRoleEnum == null){
            return joinPoint.proceed();
        }
        //必须有权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //必须有管理员权限
        if(userRoleEnum.ADMIN.equals(mustRoleEnum) && !userRoleEnum.ADMIN.equals(userRoleEnum))
        {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
