package com.example.devnote.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

// 文章 DTO
@Data
public class PostDto {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过 100 字")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
}