package com.example.devnote.service;

import com.example.devnote.entity.Favorite;
import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import com.example.devnote.repository.FavoriteRepository;
import com.example.devnote.repository.PostRepository;
import com.example.devnote.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 构造函数注入依赖（推荐方式，保证依赖不可变、线程安全）
    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository, PostRepository postRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    /**
     * 收藏 / 取消收藏
     * 当用户点击收藏按钮时，如果已收藏则取消；否则创建收藏记录。
     *
     * @param username 当前登录用户的用户名
     * @param postId   被收藏的文章ID
     */
    @Transactional // @Transactional：声明该方法的所有数据库操作要么全部成功，要么全部回滚（防止部分成功导致数据不一致）
    public void toggleFavorite(String username, Long postId) {
        // 根据用户名查找当前用户对象
        User user = userRepository.findByUsername(username);

        // 根据文章ID查找文章对象，如果不存在则抛出异常
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        // 检查该用户是否已经收藏过这篇文章
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndPost(user, post);

        if (existingFavorite.isPresent()) {
            // 如果已收藏 → 删除收藏记录（取消收藏）
            favoriteRepository.delete(existingFavorite.get());
        } else {
            // 如果未收藏 → 新建收藏记录并保存
            favoriteRepository.save(new Favorite(user, post));
        }
    }

    /**
     * 统计某篇文章的收藏数量
     * @param postId 文章ID
     * @return 收藏数
     */
    public long countFavorites(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        return favoriteRepository.countByPost(post);
    }

    /**
     * 判断用户是否已收藏该文章
     * @param username 用户名
     * @param postId 文章ID
     * @return true 表示已收藏，false 表示未收藏
     */
    public boolean isFavoritedByUser(String username, Long postId) {
        User user = userRepository.findByUsername(username);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        return favoriteRepository.findByUserAndPost(user, post).isPresent();
    }

    /**
     * 获取某个用户收藏的所有文章（用于 我的收藏 页面）
     * @param username 用户名
     * @return 用户收藏的 Favorite 列表
     */
    public List<Favorite> getUserFavorites(String username) {
        User user = userRepository.findByUsername(username);
        return favoriteRepository.findByUser(user);
    }

}
