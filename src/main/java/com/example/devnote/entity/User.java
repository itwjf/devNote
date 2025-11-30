package com.example.devnote.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)//用户名唯一
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "ROLE_USER";// 默认角色（Spring Security 规范要求前缀 ROLE_）

    /** 用户头像URL */
    private String avatar = "/images/default-avatar.png";

    /** 个人简介 */
    @Column(length = 500)
    private String bio = "这个人很神秘，什么也没写~";

    //关联关系
    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Post> posts;

    /**
     * 是否公开粉丝列表
     * true：任何人都可以查看；false：仅自己可见
     */
    private boolean showFollowers = true;

    /**
     * 是否公开关注列表
     */
    private boolean showFollowing = true;

    /**
     * 是否公开点赞列表
     */
    private boolean showLikes = false;

    /**
     * 是否公开收藏列表
     */
    private boolean showFavorites = false;

    public User() {
    }

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public boolean isShowFollowers() {
        return showFollowers;
    }

    public void setShowFollowers(boolean showFollowers) {
        this.showFollowers = showFollowers;
    }

    public boolean isShowFollowing() {
        return showFollowing;
    }

    public void setShowFollowing(boolean showFollowing) {
        this.showFollowing = showFollowing;
    }

    public boolean isShowLikes() {
        return showLikes;
    }

    public void setShowLikes(boolean showLikes) {
        this.showLikes = showLikes;
    }

    public boolean isShowFavorites() {
        return showFavorites;
    }

    public void setShowFavorites(boolean showFavorites) {
        this.showFavorites = showFavorites;
    }
}
