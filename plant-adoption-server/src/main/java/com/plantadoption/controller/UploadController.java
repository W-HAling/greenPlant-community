package com.plantadoption.controller;

import com.plantadoption.common.Result;
import com.plantadoption.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Tag(name = "文件管理", description = "文件上传相关接口")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    
    private final FileService fileService;
    
    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileService.uploadImage(file);
        return Result.success("上传成功", url);
    }
    
    @Operation(summary = "上传多张图片")
    @PostMapping("/images")
    public Result<String[]> uploadImages(@RequestParam("files") MultipartFile[] files) {
        String[] urls = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i] = fileService.uploadImage(files[i]);
        }
        return Result.success("上传成功", urls);
    }
}
