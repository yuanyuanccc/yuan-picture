package com.yuanc.yuanpicturebackend.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.yuanc.yuanpicturebackend.annotation.AuthCheck;
import com.yuanc.yuanpicturebackend.common.BaseResponse;
import com.yuanc.yuanpicturebackend.common.ResultUtils;
import com.yuanc.yuanpicturebackend.constant.UserConstant;
import com.yuanc.yuanpicturebackend.exception.BusinessException;
import com.yuanc.yuanpicturebackend.exception.ErrorCode;
import com.yuanc.yuanpicturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {


    private final CosManager cosManager;

    public FileController(CosManager cosManager) {
        this.cosManager = cosManager;
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File file = null;
        try {
            file = File.createTempFile(filepath,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath,file);
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath= "+filepath,e );
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"文件上传失败");
        }finally {
            if(file!=null){
                boolean delete = file.delete();
                if(!delete){
                    log.error("file delete error, filepath= {} ",filepath);
                }
            }
        }

    }


    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/download")
    public void testDownLoadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filepath);

            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath= "+filepath,e );
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"文件下载失败");
        }finally {
            if(cosObjectInput!=null)
            {
                cosObjectInput.close();
            }
        }

    }

}
