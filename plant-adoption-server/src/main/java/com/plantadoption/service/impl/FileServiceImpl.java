package com.plantadoption.service.impl;

import com.plantadoption.common.ErrorCode;
import com.plantadoption.config.MinioConfig;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    
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

            // 按照日期划分目录
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = datePath + "/" + UUID.randomUUID().toString().replace("-", "") + extension;

            // 检查桶是否存在并创建
            boolean isExist = minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucket).build());
            if (!isExist) {
                minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucket).build());
                // 可选：设置桶的访问策略为 public
                String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\"],\"Resource\":[\"arn:aws:s3:::" + bucket + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]}]}";
                minioClient.setBucketPolicy(io.minio.SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
            }

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            String externalUrl = minioConfig.getMinioExternalUrl();
            if (externalUrl != null && !externalUrl.isEmpty()) {
                if (externalUrl.endsWith("/")) {
                    externalUrl = externalUrl.substring(0, externalUrl.length() - 1);
                }
                return externalUrl + "/" + bucket + "/" + objectName;
            }
            
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
            
            String externalUrl = minioConfig.getMinioExternalUrl();
            if (externalUrl != null && fileUrl.startsWith(externalUrl)) {
                fileUrl = fileUrl.substring(externalUrl.length());
            }
            
            if (fileUrl.startsWith("/")) {
                fileUrl = fileUrl.substring(1);
            }
            
            int firstSlashIndex = fileUrl.indexOf("/");
            if (firstSlashIndex == -1) {
                return;
            }
            
            String bucket = fileUrl.substring(0, firstSlashIndex);
            String objectName = fileUrl.substring(firstSlashIndex + 1);
            
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("文件删除失败, URL: {}", fileUrl, e);
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
        if (contentType != null && (contentType.startsWith("image/") || contentType.equals("application/octet-stream"))) {
            isValidType = true;
        } else {
            for (String allowedType : ALLOWED_IMAGE_TYPES) {
                if (allowedType.equals(contentType)) {
                    isValidType = true;
                    break;
                }
            }
        }
        
        if (!isValidType) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ERROR);
        }
    }
}
