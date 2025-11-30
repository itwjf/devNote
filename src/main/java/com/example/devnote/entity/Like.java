package com.example.devnote.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * 点赞（喜欢）实体类
 * 用户（User）与文章（Post）之间的 多对多关系
 */
@Entity
@Table(name = "post_likes")
@Getter
@Setter
@NoArgsConstructor
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id",nullable = false)
    private Post post;

    @Column(name = "liked_at", nullable = false, columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)")
    private LocalDateTime likedAt = LocalDateTime.now();

    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
        this.likedAt = LocalDateTime.now(); // 确保新建时有值
    }


}
