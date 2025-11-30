package com.example.devnote.repository;

import com.example.devnote.entity.Favorite;
import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite,Long> {


    Optional<Favorite> findByUserAndPost(User user, Post post);

    long countByPost(Post post);
    
    List<Favorite> findByUser(User user);
    
    //统计用户收藏的文章总数
    long countByUser(User user);

}
