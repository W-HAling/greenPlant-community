package com.plantadoption.service.impl;

import com.plantadoption.common.ErrorCode;
import com.plantadoption.exception.BusinessException;
import com.plantadoption.service.FileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name:plant-images}")
    private String bucketName;
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    
    @Override
    public String uploadImage(MultipartFile file) {
        validateImageFile(file);
        return uploadFile(file, bucketName);
    }
    
    @Override
    public String uploadFile(MultipartFile file, String bucket) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
            String objectName = UUID.randomUUID().toString().replace("-", "") + extension;
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            return "/" + bucket + "/" + objectName;
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return;
            }
            
            String[] parts = fileUrl.split("/");
            if (parts.length < 3) {
                return;
            }
            
            String bucket = parts[1];
            String objectName = parts[2];
            
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("文件删除失败", e);
        }
    }
    
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ERROR);
        }
    }
}
