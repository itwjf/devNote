package com.example.devnote.integration;

import com.example.devnote.entity.User;
import com.example.devnote.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户相关功能的集成测试（简化版）
 * 使用 H2 内存数据库进行测试
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("用户集成测试（简化版）")
class UserIntegrationTestSimple {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("用户注册流程 - 成功")
    void userRegistration_Success() throws Exception {
        // When & Then - 执行注册请求
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "newuser")
                .param("password", "password123")
                .param("email", "newuser@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("message"));

        // 验证用户已保存到数据库
        User savedUser = userRepository.findByUsername("newuser");
        assertNotNull(savedUser);
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("newuser@example.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        assertEquals("USER", savedUser.getRole());
    }

    @Test
    @DisplayName("用户注册流程 - 用户名已存在")
    void userRegistration_UsernameExists() throws Exception {
        // Given - 先创建一个用户
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setEmail("existing@example.com");
        existingUser.setRole("USER");
        userRepository.save(existingUser);

        // When & Then - 尝试注册相同用户名
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "existinguser")
                .param("password", "password123")
                .param("email", "newemail@example.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("用户注册流程 - 密码过短")
    void userRegistration_PasswordTooShort() throws Exception {
        // When & Then - 尝试使用短密码注册
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "newuser")
                .param("password", "123") // 少于6位
                .param("email", "newuser@example.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("访问受保护页面 - 未登录用户重定向")
    void accessProtectedPage_UnauthenticatedUser() throws Exception {
        // When & Then - 未登录用户尝试访问需要认证的页面
        mockMvc.perform(get("/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("访问受保护页面 - 已登录用户成功访问")
    void accessProtectedPage_AuthenticatedUser() throws Exception {
        // Given - 创建并登录用户
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
        userRepository.save(testUser);

        // When & Then - 已登录用户访问受保护页面
        mockMvc.perform(get("/new")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser")))
                .andExpect(status().isOk())
                .andExpect(view().name("new"));
    }

    @Test
    @DisplayName("用户资料更新 - 成功")
    void userProfileUpdate_Success() throws Exception {
        // Given - 创建测试用户
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
        testUser.setBio("原始简介");
        userRepository.save(testUser);

        // When & Then - 更新用户资料
        mockMvc.perform(post("/user/edit")
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser"))
                .param("email", "updated@example.com")
                .param("bio", "更新后的简介")
                .param("avatar", "/images/new-avatar.png"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/testuser"));

        // 验证数据库中的更新
        User updatedUser = userRepository.findByUsername("testuser");
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("更新后的简介", updatedUser.getBio());
        assertEquals("/images/new-avatar.png", updatedUser.getAvatar());
    }

    @Test
    @DisplayName("用户隐私设置更新 - 成功")
    void userPrivacySettingsUpdate_Success() throws Exception {
        // Given - 创建测试用户
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
        testUser.setShowFollowers(true);
        testUser.setShowFollowing(true);
        testUser.setShowLikes(true);
        testUser.setShowFavorites(true);
        userRepository.save(testUser);

        // When & Then - 更新隐私设置
        mockMvc.perform(post("/user/privacy")
                .with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser"))
                .param("showFollowers", "false")
                .param("showFollowing", "false")
                .param("showLikes", "false")
                .param("showFavorites", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/privacy"));

        // 验证数据库中的更新
        User updatedUser = userRepository.findByUsername("testuser");
        assertFalse(updatedUser.isShowFollowers());
        assertFalse(updatedUser.isShowFollowing());
        assertFalse(updatedUser.isShowLikes());
        assertFalse(updatedUser.isShowFavorites());
    }
}