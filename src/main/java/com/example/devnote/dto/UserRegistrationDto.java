package com.example.devnote.dto;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Email;

// 定义用户注册的 DTO
public class UserRegistrationDto {
    
    // 用户名
    @NotBlank(message = "用户名不能为空")
    private String username;

    // 密码
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码至少 8 位")
    private String password;
    
    // getters / setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // 确认密码
    @NotBlank(message = "确认密码不能为空")
    @Size(min = 8, message = "确认密码至少 8 位")
    private String confirmPassword;

    // 邮箱地址
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}