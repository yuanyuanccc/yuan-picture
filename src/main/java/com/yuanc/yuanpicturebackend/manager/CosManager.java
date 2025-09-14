package com.yuanc.yuanpicturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.yuanc.yuanpicturebackend.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;


    /**
     * 上传对象
     * @param key
     * @param file
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    public COSObject getObject(String key)
    {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }


/**
 * 上传文件到对象存储(附带图片信息)
 * @param key 文件在对象存储中的键名
 * @param file 要上传的本地文件对象
 * @return PutObjectResult 包含文件上传结果的对象
 */
    public PutObjectResult putPictureObject(String key, File file) {
        // 创建上传请求对象，指定存储桶名称、文件键名和本地文件
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 创建图片操作对象，用于设置图片处理相关参数
        PicOperations picOperations = new PicOperations();

        // 设置获取图片信息标志为1，表示需要获取图片的基本信息
        picOperations.setIsPicInfo(1);


        List<PicOperations.Rule> rules = new ArrayList<>();
        //图片压缩（转成webp格式）
        String webpKey = FileUtil.mainName(key)  +  ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);

        //缩略图处理 仅对大于20kb的图片生成缩略图
        if(file.length() > 2 * 1024){
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            //拼接缩略图的路径
            String thumbnailKey = FileUtil.mainName(key)  +  "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",256,256));
            rules.add(thumbnailRule);
        }
        //构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);

        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 删除对象
     * @param key
     * @return
     */
    public void deleteObject(String key)
    {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

}

