package com.example.devnote.service;

import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import com.example.devnote.repository.PostRepository;
import com.example.devnote.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {


    private UserRepository userRepository;


    private PostRepository postRepository;

    public UserService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }
    
    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    

    
    /**
     * 查找用户的文章，支持分页并考虑权限
     */
    public Page<Post> findPostsByUser(User user, User currentUser, int page, int pageSize) {
        PageRequest pageable = PageRequest.of(page, pageSize);
        
        // 如果是用户本人或者没有登录用户，显示所有文章
        if (currentUser == null || currentUser.getId().equals(user.getId())) {
            return postRepository.findByAuthorOrderByCreatedAtDesc(user, pageable);
        } else {
            // 否则只显示公开文章和粉丝可见文章
            return postRepository.findByAuthorAndVisibilityInOrderByCreatedAtDesc(
                user, 
                List.of("PUBLIC", "FOLLOWERS"), 
                pageable
            );
        }
    }


    /**
     * 查询用户发表的所有文章（按时间倒序）
     */
    public List<Post> findPostsByUser(User user) {
        return postRepository.findByAuthorOrderByCreatedAtDesc(user);
    }
    
    /**
     * 统计用户发布的文章总数
     * @param user 用户对象
     * @return 文章总数
     */
    public long countUserPosts(User user) {
        return postRepository.countByAuthor(user);
    }

    /**
     * 更新用户隐私设置
     * @param username 用户名
     * @param showFollowers 是否公开粉丝列表
     * @param showFollowing 是否公开关注列表
     * @param showLikes 是否公开点赞列表
     * @param showFavorites 是否公开收藏列表
     */
    public void updatePrivacySettings(String username,
                                      boolean showFollowers,
                                      boolean showFollowing,
                                      boolean showLikes,
                                      boolean showFavorites) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setShowFollowers(showFollowers);
            user.setShowFollowing(showFollowing);
            user.setShowLikes(showLikes);
            user.setShowFavorites(showFavorites);
            userRepository.save(user);
        }
    }
}
