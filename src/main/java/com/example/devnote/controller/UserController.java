package com.example.devnote.controller;

import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import com.example.devnote.repository.UserRepository;
import com.example.devnote.service.FollowService;
import com.example.devnote.service.PostService;
import com.example.devnote.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@Controller
public class UserController {


    private final UserService userService;

    private final UserRepository userRepository;

    private final FollowService followService;

    private final PostService postService;

    public UserController(UserService userService, UserRepository userRepository, FollowService followService, PostService postService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.followService = followService;
        this.postService = postService;
    }

    @Value("${file.upload-dir}")
    private String uploadDir;// 相对路径，例如 "uploads/avatar/"

    @Value("${file.absolute-path:}") // 可选项，服务器使用
    private String absolutePath;


    
    /**
     * 用户主页：显示个人信息与文章列表
     */
    @GetMapping("/user/{username}")
    public String userProfile(@PathVariable String username,
                              Authentication authentication,
                              Model model) {
        //查询用户是否存在
        User user = userService.findByUsername(username);
        if (user == null) {
            model.addAttribute("error", "用户不存在");
            return "error";
        }

        // 获取当前登录用户
        User currentUser = getLoggedInUser(authentication);

        String currentUsername = (currentUser != null) ? currentUser.getUsername() : null;
        model.addAttribute("currentUsername", currentUsername);

        boolean isSelf = currentUser != null && currentUser.getId().equals(user.getId());

        boolean isFollowing = false;
        if (!isSelf && currentUser != null) {
            isFollowing = followService.isFollowing(currentUsername, username);
        }

        //统计粉丝数与关注数
        long followersCount = followService.countFollowers(username);
        long followingCount = followService.countFollowing(username);
        
        // 统计文章、点赞、收藏总数
        long totalPosts = userService.countUserPosts(user);
        long totalLikedPosts = postService.countLikedPosts(username);
        long totalFavoritedPosts = postService.countFavoritedPosts(username);

        model.addAttribute("user", user);
        model.addAttribute("isSelf", isSelf);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("followersCount", followersCount);
        model.addAttribute("followingCount", followingCount);
        
        // 文章统计信息
        model.addAttribute("totalPosts", totalPosts);
        model.addAttribute("totalLikedPosts", totalLikedPosts);
        model.addAttribute("totalFavoritedPosts", totalFavoritedPosts);

        return "user_profile";
    }




    


    @GetMapping("/user/{username}/edit")
    public String editProfile(@PathVariable String username,Authentication authentication,Model model){

        if (authentication == null || !authentication.isAuthenticated()){
            return "redirect:/login";
        }

        // 仅允许本人编辑
        if (!authentication.getName().equals(username)) {
            return "redirect:/user/" + username + "?error=forbidden";
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            return "redirect:/error";
        }

        model.addAttribute("user", user);
        return "user_edit_profile";  // 进入编辑页
    }

    @PostMapping("/user/{username}/edit")
    public String updateProfile(
            @PathVariable String username,
            @RequestParam(value = "bio",required = false) String bio,
            @RequestParam(value = "avatar",required = false) MultipartFile avatarFile,
            Model model
    ) {
        User user = userRepository.findByUsername(username);

        user.setBio(bio);

        try {
            // 如果用户上传了头像
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String filename = System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();

                // 确保目录存在
                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // 保存文件到本地
                File dest = new File(dir, filename);
                avatarFile.transferTo(dest);

                // 在数据库中保存相对路径（方便 Thymeleaf 显示）
                user.setAvatar("/uploads/" + filename);
            }

            userRepository.save(user);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "头像上传失败，请重试！");
            return "user_edit_profile";
        }
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);

        return "redirect:/user/" + encodedUsername;
    }

    // 粉丝列表
    @GetMapping("/user/{username}/followers")
    public String viewFollowersPage(
            @PathVariable String username,
            Authentication authentication,
            Model model,
            @PageableDefault(size = 20) Pageable pageable) {

        User targetUser = userService.findByUsername(username);

        if (targetUser == null) {
            model.addAttribute("errorCode", "404");
            model.addAttribute("errorMessage", "用户不存在");
            return "error";
        }

        User currentUser = getLoggedInUser(authentication);
        boolean isSelf = currentUser != null && currentUser.getId().equals(targetUser.getId());

        // 🔒 权限检查：非本人 且 粉丝列表未公开 → 拒绝访问
        if (!isSelf && !targetUser.isShowFollowers()) {
            model.addAttribute("errorCode", "403");
            model.addAttribute("errorMessage", "该用户未公开粉丝列表");
            return "error";
        }

        // ✅ 通过权限检查后，才加载数据
        Page<User> followersPage = followService.getFollowersPage(username, currentUser, pageable);

        model.addAttribute("profileUser", targetUser);
        model.addAttribute("followersPage", followersPage);
        model.addAttribute("isSelf", isSelf);
        return "user_followers";
    }

    @GetMapping("/user/{username}/following")
    public String viewFollowingPage(
            @PathVariable String username,
            Authentication authentication,
            Model model,
            @PageableDefault(size = 20) Pageable pageable) {

        User targetUser = userService.findByUsername(username);

        if (targetUser == null) {
            model.addAttribute("errorCode", "404");
            model.addAttribute("errorMessage", "用户不存在");
            return "error";
        }

        User currentUser = getLoggedInUser(authentication);
        boolean isSelf = currentUser != null && currentUser.getId().equals(targetUser.getId());

        // 🔒 权限检查：非本人 且 未公开 → 拒绝访问
        if (!isSelf && !targetUser.isShowFollowing()) {
            model.addAttribute("errorCode", "403");
            model.addAttribute("errorMessage", "该用户未公开关注列表");
            return "error";
        }

        // ✅ 加载数据
        Page<User> followingPage = followService.getFollowingPage(username, currentUser, pageable);

        model.addAttribute("profileUser", targetUser);
        model.addAttribute("followingPage", followingPage);
        model.addAttribute("isSelf", isSelf);
        return "user_following";
    }

    /**
     * 显示用户点赞的文章列表
     */
    @GetMapping("/user/{username}/liked-posts")
    public String viewLikedPostsPage(
            @PathVariable String username, // 目标用户名
            Authentication authentication, // 当前登录用户认证信息
            Model model, // 模型传递数据到视图
            @PageableDefault(size = 5) Pageable pageable) // 分页参数
    {

        // 查找目标用户
        User targetUser = userService.findByUsername(username);

        // 用户不存在处理
        if (targetUser == null) {
            model.addAttribute("errorCode", "404");
            model.addAttribute("errorMessage", "用户不存在");
            return "error";
        }

        // 获取当前登录用户
        User currentUser = getLoggedInUser(authentication);
        // 判断是否为本人
        boolean isSelf = currentUser != null && currentUser.getId().equals(targetUser.getId());

        // 权限检查：非本人 且 点赞列表未公开 → 拒绝访问
        if (!isSelf && !targetUser.isShowLikes()) {
            model.addAttribute("errorCode", "403");
            model.addAttribute("errorMessage", "该用户未公开点赞列表");
            return "error";
        }


        // 使用现有的liked_posts.html模板
        model.addAttribute("user", targetUser);
        model.addAttribute("page", pageable.getPageNumber() + 1); // 前端模板使用page变量
        model.addAttribute("isFavoritedPage", false); // 标记这是点赞页面

        return "liked_posts"; // 使用现有的模板
    }

    /**
     * 显示用户收藏的文章列表
     */
    @GetMapping("/user/{username}/favorited-posts")
    public String viewFavoritedPostsPage(
            @PathVariable String username,
            Authentication authentication,
            Model model,
            @PageableDefault(size = 5) Pageable pageable) {

        User targetUser = userService.findByUsername(username);
        if (targetUser == null) {
            model.addAttribute("errorCode", "404");
            model.addAttribute("errorMessage", "用户不存在");
            return "error";
        }

        User currentUser = getLoggedInUser(authentication);
        boolean isSelf = currentUser != null && currentUser.getId().equals(targetUser.getId());

        // 权限检查：非本人 且 收藏列表未公开 → 拒绝访问
        if (!isSelf && !targetUser.isShowFavorites()) {
            model.addAttribute("errorCode", "403");
            model.addAttribute("errorMessage", "该用户未公开收藏列表");
            return "error";
        }


        // 使用现有的liked_posts.html模板作为基础，但添加不同的标题和内容
        model.addAttribute("user", targetUser);
        model.addAttribute("page", pageable.getPageNumber() + 1);
        model.addAttribute("isFavoritedPage", true); // 标记这是收藏页面
        
        return "liked_posts"; // 复用现有的模板
    }

    // 工具方法：从 Authentication 获取当前用户
    private User getLoggedInUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return userService.findByUsername(authentication.getName());
        }
        return null;
    }


}