package com.example.devnote.service;


import com.example.devnote.entity.Like;
import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import com.example.devnote.repository.LikeRepository;
import com.example.devnote.repository.PostRepository;
import com.example.devnote.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    //
    /**
     * 点赞/取消点赞
     * @Transactional :确保数据库操作的事务一致性：
     *                 如果执行过程中出错，整个方法会自动回滚。
     */
    @Transactional      //告诉Spring这个方法里的所有数据库操作要么全部成功，要么全部回滚
    public void toggleLike(String username, Long postId) {
        User user = userRepository.findByUsername(username);
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("文章不存在"));


        // 判断用户是否已经对该文章点过赞
        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);


        if (existingLike.isPresent()) {
            //点过则删除点赞记录（取消点赞）
            likeRepository.delete(existingLike.get());
        } else {
            //没点过则新建一个点赞记录（点赞）
            likeRepository.save(new Like(user, post));
        }
    }

    /**
     * 获取文章点赞数
     */
    public long countLikes(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("文章不存在"));
        return likeRepository.countByPost(post);
    }

    /**
     * 判断用户是否已点赞
     */
    public boolean isLikedByUser(String username, Long postId) {
        User user = userRepository.findByUsername(username);
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("文章不存在"));
        return likeRepository.findByUserAndPost(user, post).isPresent();
    }
}
