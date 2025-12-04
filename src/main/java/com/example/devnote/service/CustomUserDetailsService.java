package com.example.devnote.service;

import com.example.devnote.entity.User;
import com.example.devnote.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义用户信息加载服务
 *
 * 作用：
 *  - 当用户登录时，Spring Security 会自动调用此类
 *  - 通过用户名从数据库中加载用户信息（密码、角色等）
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if(user == null){
            throw  new UsernameNotFoundException("用户不存在：" + username);
        }

        // Spring Security 内置的 User 对象（不同于我们自己的 User 实体类）
        // 这里把数据库中的用户转换成框架能识别的形式
        // 注意：User.withUsername().roles(...) 会自动为角色添加 "ROLE_" 前缀，
        // 因此我们需要传入不带前缀的角色名（例如 "USER"）。
        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "USER"; // 默认角色
        } else {
            role = role.trim();
            if (role.startsWith("ROLE_")) {
                role = role.substring("ROLE_".length());
            }
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(role)
                .build();

    }
}
