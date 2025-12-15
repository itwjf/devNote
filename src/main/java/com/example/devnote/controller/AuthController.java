package com.example.devnote.controller;

import com.example.devnote.dto.UserRegistrationDto;
import com.example.devnote.exception.UserAlreadyExistsException;
import com.example.devnote.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import javax.validation.Valid;

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

    /**
     * 注册用户
     * @Valid 注解用于验证 UserRegistrationDto 对象的字段是否符合要求
     * BindingResult 对象用于存储验证结果
     * Model 对象用于向视图传递数据
     * @param userDto 用户注册信息
     * @param bindingResult 验证结果
     * @param model 模型
     * 
     */
    @PostMapping("/register")
    public String registerUser(@Valid UserRegistrationDto userDto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        System.out.println("===> 收到注册请求: " + userDto.getUsername());
        try {
            userService.register(userDto.getUsername(),
                                 userDto.getPassword(),
                                 userDto.getEmail());
            System.out.println("===> 用户保存成功: " + userDto.getUsername());
        } catch (UserAlreadyExistsException e) {
            System.out.println("===> 用户已存在: " + userDto.getUsername());
            model.addAttribute("exists", true);
            return "register";
        } catch (IllegalArgumentException e) {
            System.out.println("===> 注册参数错误: " + e.getMessage());
            model.addAttribute("invalidEmail", true);
            return "register";
        } catch (Exception e) {
            System.out.println("===> 保存用户时出错:");
            e.printStackTrace();
            model.addAttribute("error", true);
            return "register";
        }
        return "redirect:/login?success";
    }

}
