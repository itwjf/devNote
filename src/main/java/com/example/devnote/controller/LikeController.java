package com.example.devnote.controller;


import com.example.devnote.service.LikeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/{postId}")
    public Map<String, Object> toggleLike(@PathVariable Long postId, Authentication authentication) {
        String username = authentication.getName();
        // 调用 Service 层执行点赞/取消点赞逻辑（数据库更新）
        likeService.toggleLike(username, postId);

        long likeCount = likeService.countLikes(postId);
        boolean isLiked = likeService.isLikedByUser(username, postId);

        // 构造返回 JSON 数据
        Map<String, Object> response = new HashMap<>();
        response.put("liked", isLiked);
        response.put("likeCount", likeCount);

        // 返回 JSON 响应给前端（前端 AJAX 会根据这个更新页面）
        return response;
    }

    /**
     * 用于获取点赞状态 和点赞数量
     *
     */
    @GetMapping("/status/{postId}")
    public Map<String, Object> getLikeStatus(@PathVariable Long postId, Authentication authentication) {

        //authentication
        //Spring Security 自动注入的用户认证信息，若未登录则为 null。
        String username = authentication != null ? authentication.getName() : null;

        long likeCount = likeService.countLikes(postId);
        boolean isLiked = false;

        // 如果用户已登录，则判断是否已点赞
        if (username != null) {
            isLiked = likeService.isLikedByUser(username, postId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("liked", isLiked);
        response.put("likeCount", likeCount);
        return response;
    }

}
