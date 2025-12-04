package com.example.devnote.service;

import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;


/**
 * UserService.java
 * 这个接口定义了与用户相关的操作，包括查找用户、获取用户的帖子、更新隐私设置和注册新用户。
 * 它提供了方法来根据用户名查找用户，获取用户的帖子列表，计算用户的帖子数量，更新用户的隐私设置，以及注册新用户。
 * 这个接口的实现类将处理这些操作的具体逻辑。
 */
public interface UserService {

    // 根据用户名查找用户
    User findByUsername(String username);

    // 获取用户的帖子列表
    Page<Post> findPostsByUser(User user, User currentUser, int page, int pageSize);

    // 获取用户的帖子列表
    List<Post> findPostsByUser(User user);

    // 计算用户的帖子数量
    long countUserPosts(User user);

    // 更新用户隐私设置
    void updatePrivacySettings(String username,
                               boolean showFollowers,
                               boolean showFollowing,
                               boolean showLikes,
                               boolean showFavorites);

    // 注册新用户
    User register(String username, String rawPassword, String email);

}
