package com.example.devnote.controller;

import com.example.devnote.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private  final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    /**
     * 关注用户
     */
    @PostMapping("/{username}")
    public Map<String, Object> followUser(@PathVariable String username,
                                          @AuthenticationPrincipal UserDetails currentUser) {
        Map<String, Object> response = new HashMap<>();
        try {
            followService.follow(currentUser.getUsername(), username);
            response.put("success", true);
            response.put("message", "关注成功");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/{username}")
    public Map<String, Object> unfollowUser(@PathVariable String username,
                                            @AuthenticationPrincipal UserDetails currentUser) {
        Map<String, Object> response = new HashMap<>();
        try {
            followService.unfollow(currentUser.getUsername(), username);
            response.put("success", true);
            response.put("message", "取消关注成功");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

}
