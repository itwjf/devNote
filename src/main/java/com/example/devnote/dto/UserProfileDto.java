package com.example.devnote.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

// 用户资料 DTO
@Data
public class UserProfileDto {
    @NotBlank(message = "显示名称不能为空")
    @Size(max = 50, message = "显示名称不能超过 50 字")
    private String displayName;
    @Size(max = 200, message = "个人简介不能超过 200 字")
    private String bio;

    /**
     * 头像文件，前端上传 MultipartFile
     */
    private MultipartFile avatarFile;
}