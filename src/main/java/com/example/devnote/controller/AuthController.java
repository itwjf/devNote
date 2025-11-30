package com.example.devnote.controller;

import com.example.devnote.entity.User;
import com.example.devnote.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage(){
            return "login";
    }


    @GetMapping("/register")
    public String registerPage(){
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String password) {
        System.out.println("===> 收到注册请求: " + username);

        if (userRepository.findByUsername(username) != null) {
            System.out.println("===> 用户已存在: " + username);
            return "redirect:/register?error"; // 用户名已存在
        }

        // 保存用户，密码加密
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        try {
            userRepository.save(user);
            System.out.println("===> 用户保存成功: " + username);
        } catch (Exception e) {
            System.out.println("===> 保存用户时出错:");
            e.printStackTrace();
        }

        return "redirect:/login?success";
    }

}
