package com.example.devnote.service;

import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import com.example.devnote.exception.UserAlreadyExistsException;
import com.example.devnote.repository.PostRepository;
import com.example.devnote.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 实现 UserService 接口，提供用户相关的业务逻辑
@Service
public class UserServiceImpl implements UserService {

    // 注入 UserRepository
    private final UserRepository userRepository;
    // 注入 PostRepository
    private final PostRepository postRepository;
    // 注入 PasswordEncoder
    private final PasswordEncoder passwordEncoder;

    // 构造函数注入依赖
    public UserServiceImpl(UserRepository userRepository, PostRepository postRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 根据用户名查找用户
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 根据用户名查找用户，并分页返回用户的帖子
    @Override
    public Page<Post> findPostsByUser(User user, User currentUser, int page, int pageSize) {
        PageRequest pageable = PageRequest.of(page, pageSize);
        if (currentUser == null || currentUser.getId().equals(user.getId())) {
            return postRepository.findByAuthorOrderByCreatedAtDesc(user, pageable);
        } else {
            return postRepository.findByAuthorAndVisibilityInOrderByCreatedAtDesc(
                user,
                List.of("PUBLIC", "FOLLOWERS"),
                pageable
            );
        }
    }

    @Override
    public List<Post> findPostsByUser(User user) {
        return postRepository.findByAuthorOrderByCreatedAtDesc(user);
    }

    @Override
    public long countUserPosts(User user) {
        return postRepository.countByAuthor(user);
    }

    @Override
    public void updatePrivacySettings(String username, boolean showFollowers, boolean showFollowing, boolean showLikes, boolean showFavorites) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setShowFollowers(showFollowers);
            user.setShowFollowing(showFollowing);
            user.setShowLikes(showLikes);
            user.setShowFavorites(showFavorites);
            userRepository.save(user);
        }
    }

    // 注册新用户
    @Override
    @Transactional // 事务注解，确保数据一致性
    public User register(String username, String rawPassword, String email) {
        if (userRepository.findByUsername(username) != null) {
            throw new UserAlreadyExistsException(username);
        }

        // 基本校验：密码长度
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("密码过短");
        }

        // 邮箱非空检查与格式校验（注册时必须提供邮箱）
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        String emailTrim = email.trim();
        // 简单邮箱正则校验
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!emailTrim.matches(emailRegex)) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        // 密码加密
        String encoded = passwordEncoder.encode(rawPassword);
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoded);
        user.setEmail(emailTrim);

        try {
            // 保存用户
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // 如果用户名已存在，抛出自定义异常
            throw new UserAlreadyExistsException(username);
        }
    }
}
