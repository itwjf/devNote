package com.example.devnote.controller;


import com.example.devnote.entity.Comment;
import com.example.devnote.entity.User;
import com.example.devnote.repository.CommentRepository;
import com.example.devnote.repository.PostRepository;
import com.example.devnote.repository.UserRepository;
import com.example.devnote.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/posts")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    /**
     * 添加评论（登录后可用）
     */
    @PostMapping("/{postId}/comments")
    public String addComment(
            @PathVariable Long postId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId,
            Authentication authentication
    ) {
        // 未登录 → 跳转登录页
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        // 获取当前用户
        User user = userRepository.findByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/login";
        }

        // 添加评论
        commentService.addComment(postId, content, user, parentId);

        // 成功后跳回详情页
        return "redirect:/posts/" + postId;
    }

    /**
     * 删除评论（仅评论作者或文章作者可删除）
     */
    @PostMapping("/{postId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        // 未登录 → 跳转登录页
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        // 当前登录用户
        User currentUser = userRepository.findByUsername(authentication.getName());
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 删除评论（内部做权限校验）
        commentService.deleteComment(commentId, currentUser);

        // 删除成功后跳回详情页
        return "redirect:/posts/" + postId;
    }
}
