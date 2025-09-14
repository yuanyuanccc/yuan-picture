package com.yuanc.yuanpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.yuanc.yuanpicturebackend.exception.ErrorCode;
import com.yuanc.yuanpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        // 校验文件是否为空
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR,"上传文件不能为空");
        //1.校验文件大小
        long fileSize = multipartFile.getSize();
        // 定义1M的大小（字节）
        final long ONE_M = 1024 * 1024;
        // 校验文件大小是否超过2M
        ThrowUtils.throwIf(fileSize > 2*ONE_M, ErrorCode.PARAMS_ERROR,"上传文件大小不能超过2M");
        //2.校验文件后缀
        // 获取文件后缀名
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //允许上传的文件列表
        final List<String> ALLOW_FROMAT_LIST = Arrays.asList("png", "jpg", "jpeg", "webp");
        ThrowUtils.throwIf(!ALLOW_FROMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR,"上传文件格式不正确");

    }
    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return  multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
