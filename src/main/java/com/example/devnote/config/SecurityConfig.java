package com.example.devnote.config;

import com.example.devnote.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置类
 * 作用：定义哪些路径需要认证，哪些路径可以匿名访问
 */
@Configuration
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 定义密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 定义认证提供者（使用自定义的 UserDetailsService）
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 放行登录、注册、静态资源
                        .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                        //允许未登录用户查看文章（首页，详情页，错误页面）
                        .requestMatchers("/","/posts/**").permitAll()
                        //写文章必须登录
                        .requestMatchers("/posts/new","/posts/save","/posts/*/edit").authenticated()
                        // 其他请求都需要登录
                        .anyRequest().authenticated()
                )
                // 登录配置
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/",true)//登录成功跳转到首页
                        .permitAll()
                )
                // 登出配置
                .logout(logout -> logout
                        .logoutUrl("/logout")// 请求地址
                        .logoutSuccessUrl("/")// 退出后跳转页面
                        .invalidateHttpSession(true)// 清空 session
                        .deleteCookies("JSESSIONID") // 删除 cookie
                        .permitAll()
                )
                // 关闭 CSRF（开发阶段）
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
