package com.example.devnote.service;

import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import com.example.devnote.repository.FavoriteRepository;
import com.example.devnote.repository.LikeRepository;
import com.example.devnote.repository.PostRepository;
import com.example.devnote.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final LikeRepository likeRepository;

    private final FavoriteRepository favoriteRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, LikeRepository likeRepository, FavoriteRepository favoriteRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 获取点赞文章列表（不分页）
     * @param username
     * @return
     */
    public List<Post> getLikedPosts(String username) {
        User user = userRepository.findByUsername(username);
        return likeRepository.findByUser(user).stream()
                .map(like -> like.getPost())
                .collect(Collectors.toList());
    }

    /**
     * 获取点赞文章列表（分页）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页后的文章列表
     */
    public Page<Post> findLikedPostsByUserId(Long userId, Pageable pageable) {
        return postRepository.findLikedPostsByUserId(userId, pageable);
    }

    /**
     * 获取收藏文章列表（不分页）
     * @param username
     * @return
     */
    public List<Post> getFavoritedPosts(String username) {
        User user = userRepository.findByUsername(username);
        return favoriteRepository.findByUser(user).stream()
                .map(favorite -> favorite.getPost())
                .collect(Collectors.toList());
    }


    
    /**
     * 统计用户点赞的文章总数
     * @param username 用户名
     * @return 点赞文章总数
     */
    public long countLikedPosts(String username) {
        User user = userRepository.findByUsername(username);
        return likeRepository.countByUser(user);
    }
    
    /**
     * 统计用户收藏的文章总数
     * @param username 用户名
     * @return 收藏文章总数
     */
    public long countFavoritedPosts(String username) {
        User user = userRepository.findByUsername(username);
        return favoriteRepository.countByUser(user);
    }
    

    

}

