package com.yuanc.yuanpicturebackend.model.dto.picture;

import lombok.Data;

@Data
public class PictureUploadByBatchRequest {  
  
    /**  
     * 搜索词  
     */  
    private String searchText;


    /**
     * 图片名称前缀
     */
    private String namePrefix;

    /**  
     * 抓取数量  
     */  
    private Integer count = 10;  
}
