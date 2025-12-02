package com.example.devnote.repository;

import com.example.devnote.entity.Like;
import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like,Long> {

    //查询某个用户是否已点赞某篇文章
//   Optional 防止空指针，返回一个可选值。
    Optional<Like> findByUserAndPost(User user, Post post);

    //统计一篇文章的点赞数（用于显示在页面上）
    long countByPost(Post post);

    //查找用户点赞过的文章
    List<Like> findByUser(User user);
    
    //统计用户点赞的文章总数
    long countByUser(User user);
    
}
