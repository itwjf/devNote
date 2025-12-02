package com.example.devnote.controller;


import com.example.devnote.dto.PostSummaryDto;
import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import com.example.devnote.service.LikeService;
import com.example.devnote.service.PostService;
import com.example.devnote.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController //标记这是一个 REST 控制器
@RequestMapping("/api") //API 前缀
public class LikePostApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private LikeService likeService;

    // 获取用户点赞的文章列表（分页）
    @GetMapping("/user/{username}/liked-posts")
    public ResponseEntity<?> getLikedPosts( // API 设计：返回 ResponseEntity，包含状态码和数据
            @PathVariable String username, // 目标用户名
            Authentication authentication, // 当前登录用户的认证信息
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) // 默认每页5条，按ID降序排序
    {

        // 查找目标用户
        User targetUser = userService.findByUsername(username);
        // 用户不存在则返回 404
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = null;
        // 如果已登录，获取当前用户信息
        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = userService.findByUsername(authentication.getName());
        }

        // 检查隐私设置
        boolean isSelf = currentUser != null && currentUser.getId().equals(targetUser.getId());
        // 如果不是自己查看，且目标用户未公开点赞列表，则返回 403
        if (!isSelf && !targetUser.isShowLikes()) {
            return ResponseEntity.status(403).body(Map.of("error", "该用户未公开点赞列表"));
        }

        // 获取分页的点赞文章（Post 实体）
        Page<Post> page = postService.findLikedPostsByUserId(targetUser.getId(), pageable);

        // 转换为安全的 DTO，避免序列化循环引用
        List<PostSummaryDto> dtos = page.getContent().stream()
                .map(post -> new PostSummaryDto(post.getId(), post.getTitle()))
                .toList();

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("content", dtos);
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber() + 1);
        response.put("hasNext", page.hasNext());
        response.put("hasPrevious", page.hasPrevious());
        response.put("totalElements", page.getTotalElements());

        // 返回 200 和数据
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/posts/{postId}/unlike")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId, Authentication authentication) {
        // 确保用户已登录
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();

        try {
            // 直接调用 toggleLike —— 如果已点赞，就会被取消
            likeService.toggleLike(username, postId);
            // 返回 200 表示操作成功
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // 返回 400 和错误信息
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "操作失败: " + e.getMessage()));
        }

    }
}
