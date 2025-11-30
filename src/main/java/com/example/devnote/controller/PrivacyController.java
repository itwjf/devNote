package com.example.devnote.controller;

import com.example.devnote.entity.User;
import com.example.devnote.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/settings/privacy")
public class PrivacyController {

    private final UserService userService;

    public PrivacyController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 展示隐私设置页面
     */
    @GetMapping
    public String showPrivacySettings(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "settings/privacy";
    }

    /**
     * 处理隐私设置表单提交
     */
    @PostMapping("/update")
    public String updatePrivacySettings(@RequestParam(defaultValue = "false") boolean showFollowers,
                                        @RequestParam(defaultValue = "false") boolean showFollowing,
                                        @RequestParam(defaultValue = "false") boolean showLikes,
                                        @RequestParam(defaultValue = "false") boolean showFavorites,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        userService.updatePrivacySettings(
                principal.getName(),
                showFollowers,
                showFollowing,
                showLikes,
                showFavorites
        );
        redirectAttributes.addFlashAttribute("successMessage", "隐私设置已更新！");
        return "redirect:/settings/privacy";
    }
}
