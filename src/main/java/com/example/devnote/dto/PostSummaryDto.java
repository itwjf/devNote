package com.example.devnote.dto;

public class PostSummaryDto {
    private Long id;
    private String title;
    // 不包含 author 对象！只放必要字段

    public PostSummaryDto(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    // getters
    public Long getId() { return id; }
    public String getTitle() { return title; }

}
