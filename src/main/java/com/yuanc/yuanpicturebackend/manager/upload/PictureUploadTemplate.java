package com.yuanc.yuanpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.yuanc.yuanpicturebackend.config.CosClientConfig;
import com.yuanc.yuanpicturebackend.exception.BusinessException;
import com.yuanc.yuanpicturebackend.exception.ErrorCode;
import com.yuanc.yuanpicturebackend.manager.CosManager;
import com.yuanc.yuanpicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 图片上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     * 上传图片的方法
     *
     * @param inputSource      上传的图片文件，使用Spring的MultipartFile类型接收
     * @param uploadPathPrefix 上传文件的路径前缀，用于指定文件存储的基础目录
     * @return UploadPictureResult 返回上传结果，包含上传成功/失败状态及相关信息
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        //1.校验文件
        validPicture(inputSource);
        //2.图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginFilename(inputSource);
        //自己拼接文件上传路径
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            //3.创建临时文件，获取文件到服务器
            file = File.createTempFile(uploadPath, null);
            //处理文件来源
            processFile(inputSource, file);
            //4.上传文件到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //5.获取图片信息对象，封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //6.获取到图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if(CollUtil.isNotEmpty(objectList)){
                //获取压缩之后得到的文件信息
                CIObject compressedCiObject = objectList.get(0);
                //缩略图默认等于压缩值
                CIObject thumbnailCiObject = compressedCiObject;
                if(objectList.size() > 1 ){
                    thumbnailCiObject = objectList.get(1);
                }
                //封装压缩图的返回结果
                return bulidResult(originalFilename, compressedCiObject,thumbnailCiObject);
            }

            return bulidResult(originalFilename, file, uploadPath, imageInfo);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //删除临时文件
            this.deleteTempFile(file);
        }
        //临时文件清理

    }

    /**
     * 封装返回结果
     *
     * @param originalFilename
     * @param compressedCiObject
     * @param thumbnailCiObject 缩略图对象
     * @return
     */
    private UploadPictureResult bulidResult(String originalFilename, CIObject compressedCiObject, CIObject thumbnailCiObject) {
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();

        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        //设置压缩图地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        //设置缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());

        return uploadPictureResult;
    }

    /**
     * 校验输入源
     *
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     *
     * @param inputSource
     * @return
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     *
     * @param inputSource
     * @param file
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;



    /**
     * 封装返回结果
     *
     * @param originalFilename
     * @param file
     * @param uploadPath
     * @param imageInfo        对象存储返回的图片信息
     * @return
     */
    private UploadPictureResult bulidResult(String originalFilename, File file, String uploadPath, ImageInfo imageInfo) {
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();

        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }


    /**
     * 删除临时文件的方法
     *
     * @param file 需要删除的文件对象
     */
    public void deleteTempFile(File file) {
        // 检查文件对象是否为null
        if (file != null) {
            // 尝试删除文件，并获取删除结果
            boolean deleteResult = file.delete();
            // 如果删除失败，记录错误日志
            if (!deleteResult) {
                log.error("file delete error, filepath= {} ", file.getAbsoluteFile());
            }
        }
    }



}
