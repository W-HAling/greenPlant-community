package com.plantadoption.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileService {
    
    /**
     * 上传图片
     */
    String uploadImage(MultipartFile file);
    
    /**
     * 上传文件
     */
    String uploadFile(MultipartFile file, String bucketName);
    
    /**
     * 删除文件
     */
    void deleteFile(String fileUrl);
}
