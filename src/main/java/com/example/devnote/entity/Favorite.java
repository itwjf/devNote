package com.example.devnote.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 收藏 实体类
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 收藏者

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post; // 被收藏的文章

    // ✅ 新增：收藏时间
    @CreationTimestamp
    @Column(name = "favorited_at",nullable = false,updatable = false)
    private LocalDateTime favoritedAt = LocalDateTime.now();

    public Favorite(User user, Post post) {
        this.user = user;
        this.post = post;
        // 自动设置收藏时间为当前时间
        this.favoritedAt = LocalDateTime.now();
    }

}
