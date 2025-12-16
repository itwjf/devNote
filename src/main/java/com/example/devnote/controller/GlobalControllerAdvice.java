package com.example.devnote.controller;

import com.example.devnote.entity.User;
import com.example.devnote.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// 全局控制器通知，向所有视图添加当前用户信息
@ControllerAdvice
public class GlobalControllerAdvice {
    private final UserService userService;

    public GlobalControllerAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addCurrentUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            User currentUser = userService.findByUsername(auth.getName());
            if (currentUser != null) {
                model.addAttribute("currentUsername", currentUser.getUsername());
                // 如果你需要头像，也可以加：
                // model.addAttribute("currentUserAvatar", currentUser.getAvatar());
            }
        } else {
            model.addAttribute("currentUsername", null);
        }
    }
}
