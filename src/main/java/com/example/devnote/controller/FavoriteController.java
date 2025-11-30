package com.example.devnote.controller;


import com.example.devnote.service.FavoriteService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController //@RestController = @Controller + @ResponseBody，表示返回的是 JSON，而不是 HTML 页面
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 构造函数注入 Service（推荐方式）
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * 收藏 / 取消收藏的接口（AJAX 请求）
     * 前端通过 POST /favorite/{postId} 触发本方法
     *
     * @param postId 文章 ID
     * @param authentication Spring Security 自动注入的认证对象，包含当前登录用户信息
     * @return JSON 格式数据 { favorited: 是否已收藏, favoriteCount: 收藏总数 }
     */
    @PostMapping("/{postId}") // ✅ @PostMapping 表示这是一个 POST 请求接口，路径为 /favorite/{postId}
    public Map<String, Object> toggleFavorite(@PathVariable Long postId, Authentication authentication) {
        // 从认证信息中获取当前用户名
        String username = authentication.getName();

        // 调用 Service 逻辑执行收藏或取消收藏
        favoriteService.toggleFavorite(username, postId);

        // 获取最新收藏状态和数量
        long count = favoriteService.countFavorites(postId);
        boolean isFavorited = favoriteService.isFavoritedByUser(username, postId);

        // 构建响应 JSON 数据
        Map<String, Object> response = new HashMap<>();
        response.put("favorited", isFavorited);
        response.put("favoriteCount", count);
        return response; // 自动转换为 JSON 返回
    }

    /**
     * ✅ 页面加载时初始化收藏状态
     * 供前端在初次进入文章详情页时调用。
     */
    @GetMapping("/status/{postId}")
    public Map<String, Object> getFavoriteStatus(@PathVariable Long postId, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;

        long favoriteCount = favoriteService.countFavorites(postId);
        boolean isFavorited = false;

        // 如果已登录，则判断是否收藏过
        if (username != null) {
            isFavorited = favoriteService.isFavoritedByUser(username, postId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("favorited", isFavorited);
        response.put("favoriteCount", favoriteCount);
        return response;
    }
}
