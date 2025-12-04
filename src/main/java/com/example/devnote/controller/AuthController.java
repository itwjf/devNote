package com.example.devnote.controller;

import com.example.devnote.service.UserService;
import com.example.devnote.exception.UserAlreadyExistsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final UserService userService;

    // 构造函数注入 UserService
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // 登录页面
    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }

    // 注册页面
    @GetMapping("/register")
    public String registerPage(){
        return "register";
    }

    // 注册用户
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email) {
        System.out.println("===> 收到注册请求: " + username);

        try {
            userService.register(username, password, email);
            System.out.println("===> 用户保存成功: " + username);
        } catch (UserAlreadyExistsException e) {
            System.out.println("===> 用户已存在: " + username);
            return "redirect:/register?exists";
        } catch (IllegalArgumentException e) {
            System.out.println("===> 注册参数错误: " + e.getMessage());
            // 邮箱格式或其它参数错误
            return "redirect:/register?invalidEmail";
        } catch (Exception e) {
            System.out.println("===> 保存用户时出错:");
            e.printStackTrace();
            return "redirect:/register?error";
        }

        return "redirect:/login?success";
    }

}
