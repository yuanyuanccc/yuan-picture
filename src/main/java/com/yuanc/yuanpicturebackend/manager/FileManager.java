package com.yuanc.yuanpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.yuanc.yuanpicturebackend.config.CosClientConfig;
import com.yuanc.yuanpicturebackend.exception.BusinessException;
import com.yuanc.yuanpicturebackend.exception.ErrorCode;
import com.yuanc.yuanpicturebackend.exception.ThrowUtils;
import com.yuanc.yuanpicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件服务
 * 已经废弃改为使用upload包的方法
 */
@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


/**
 * 上传图片的方法
 * @param multipartFile 上传的图片文件，使用Spring的MultipartFile类型接收
 * @param uploadPathPrefix 上传文件的路径前缀，用于指定文件存储的基础目录
 * @return UploadPictureResult 返回上传结果，包含上传成功/失败状态及相关信息
 */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile,String uploadPathPrefix) {
        //校验文件
        validPicture(multipartFile);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        //自己拼接文件上传路径
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        //解析结果并返回
        File file = null;
        try {
            file = File.createTempFile(uploadPath,null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight,2).doubleValue();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost()+ "/" +uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败",e );
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            //删除临时文件
            this.deleteTempFile(file);
        }
        //临时文件清理

    }



/**
 * 校验上传的图片文件是否符合要求
 * @param multipartFile 上传的文件对象
 */
    private void validPicture(MultipartFile multipartFile) {
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


    /**
     * 删除临时文件的方法
     * @param file 需要删除的文件对象
     */
    public void deleteTempFile(File file) {
        // 检查文件对象是否为null
        if(file!=null){
            // 尝试删除文件，并获取删除结果
            boolean deleteResult = file.delete();
            // 如果删除失败，记录错误日志
            if(!deleteResult){
                log.error("file delete error, filepath= {} ",file.getAbsoluteFile());
            }
        }
    }

}
