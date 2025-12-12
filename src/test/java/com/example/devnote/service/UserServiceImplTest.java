package com.example.devnote.service;

import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import com.example.devnote.exception.UserAlreadyExistsException;
import com.example.devnote.repository.PostRepository;
import com.example.devnote.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 测试用户服务的核心业务逻辑，包括注册、查找、隐私设置等功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 单元测试")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private List<Post> testPosts;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");

        // 创建测试帖子
        Post post1 = new Post();
        post1.setId(1L);
        post1.setTitle("Post 1");
        post1.setVisibility("PUBLIC");
        post1.setAuthor(testUser);

        Post post2 = new Post();
        post2.setId(2L);
        post2.setTitle("Post 2");
        post2.setVisibility("PRIVATE");
        post2.setAuthor(testUser);

        testPosts = List.of(post1, post2);
    }

    @Test
    @DisplayName("根据用户名查找用户 - 成功")
    void findByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // When
        User result = userService.findByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("根据用户名查找用户 - 用户不存在")
    void findByUsername_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        // When
        User result = userService.findByUsername("nonexistent");

        // Then
        assertNull(result);
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("注册新用户 - 成功")
    void register_Success() {
        // Given
        String username = "newuser";
        String password = "password123";
        String email = "newuser@example.com";
        String encodedPassword = "encodedPassword123";

        when(userRepository.findByUsername(username)).thenReturn(null);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.register(username, password, email);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("注册新用户 - 用户名已存在")
    void register_UserAlreadyExists() {
        // Given
        String username = "existinguser";
        when(userRepository.findByUsername(username)).thenReturn(testUser);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(username, "password123", "test@example.com");
        });

        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

@Test
    @DisplayName("注册 - 密码过短")
    void register_PasswordTooShort() {
        // Given
        String username = "newuser";
        String shortPassword = "123"; // 少于6位

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(username, shortPassword, "test@example.com");
        });

        // 验证确实调用了用户名检查（因为实现在验证密码之前先检查用户名）
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

@Test
    @DisplayName("注册 - 邮箱为空")
    void register_EmptyEmail() {
        // Given
        String username = "newuser";
        String password = "password123";
        String emptyEmail = "";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(username, password, emptyEmail);
        });

        // 验证确实调用了用户名检查（因为实现在验证邮箱之前先检查用户名）
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

@Test
    @DisplayName("注册 - 邮箱格式无效")
    void register_InvalidEmailFormat() {
        // Given
        String username = "newuser";
        String password = "password123";
        String invalidEmail = "invalid-email";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(username, password, invalidEmail);
        });

        // 验证确实调用了用户名检查（因为实现在验证邮箱之前先检查用户名）
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("更新隐私设置 - 成功")
    void updatePrivacySettings_Success() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(testUser);

        // When
        userService.updatePrivacySettings(username, false, false, true, true);

        // Then
        assertFalse(testUser.isShowFollowers());
        assertFalse(testUser.isShowFollowing());
        assertTrue(testUser.isShowLikes());
        assertTrue(testUser.isShowFavorites());
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("更新隐私设置 - 用户不存在")
    void updatePrivacySettings_UserNotFound() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(null);

        // When
        userService.updatePrivacySettings(username, false, false, true, true);

        // Then
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("获取用户帖子 - 查看自己的帖子")
    void findPostsByUser_ViewingOwnPosts() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Post> expectedPage = new PageImpl<>(testPosts, pageRequest, testPosts.size());
        
        when(postRepository.findByAuthorOrderByCreatedAtDesc(testUser, pageRequest))
            .thenReturn(expectedPage);

        // When
        Page<Post> result = userService.findPostsByUser(testUser, testUser, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(postRepository, times(1)).findByAuthorOrderByCreatedAtDesc(testUser, pageRequest);
    }

    /** 
     * 测试获取用户帖子 - 查看他人帖子
     * 当用户查看他人帖子时，只能看到对方公开的帖子
    */
    @Test
    @DisplayName("获取用户帖子 - 查看他人帖子")
    void findPostsByUser_ViewingOthersPosts() {
        // Given
        // 创建另一个用户
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Post> publicPosts = testPosts.stream()
            .filter(post -> "PUBLIC".equals(post.getVisibility()))
            .toList();
        Page<Post> expectedPage = new PageImpl<>(publicPosts, pageRequest, publicPosts.size());
        
        when(postRepository.findByAuthorAndVisibilityInOrderByCreatedAtDesc(
            eq(testUser), eq(List.of("PUBLIC", "FOLLOWERS")), any(PageRequest.class)))
            .thenReturn(expectedPage);

        // When
        Page<Post> result = userService.findPostsByUser(testUser, otherUser, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size()); // 只有公开的帖子
        verify(postRepository, times(1)).findByAuthorAndVisibilityInOrderByCreatedAtDesc(
            eq(testUser), eq(List.of("PUBLIC", "FOLLOWERS")), any(PageRequest.class));
    }

    @Test
    @DisplayName("统计用户帖子数量")
    void countUserPosts_Success() {
        // Given
        when(postRepository.countByAuthor(testUser)).thenReturn(5L);

        // When
        long result = userService.countUserPosts(testUser);

        // Then
        assertEquals(5L, result);
        verify(postRepository, times(1)).countByAuthor(testUser);
    }

    @Test
    @DisplayName("获取用户帖子列表")
    void findPostsByUser_Success() {
        // Given
        when(postRepository.findByAuthorOrderByCreatedAtDesc(testUser)).thenReturn(testPosts);

        // When
        List<Post> result = userService.findPostsByUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(postRepository, times(1)).findByAuthorOrderByCreatedAtDesc(testUser);
    }
}