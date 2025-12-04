package com.example.devnote.repository;

import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository 设计模式：封装了对数据库的操作（增删改查）
 *
 * JpaRepository<Post, Long> 是 Spring Data JPA 提供的接口
 * - 第一个泛型：操作的实体类 → Post
 * - 第二个泛型：主键类型 → Long
 *
 * 只要继承它，就自动拥有：
 * - save()     → 保存
 * - findById() → 根据 ID 查询
 * - findAll()  → 查询所有
 * - delete()   → 删除
 * - count()    → 统计数量
 *
 * 不用手写 SQL，也不用手写实现类！
 */
@Repository // 标记这是一个 Spring Bean，会被自动扫描和管理
public interface PostRepository extends JpaRepository<Post,Long> {
    // 暂时不需要写任何方法
    // 因为父接口已经提供了常用 CRUD 操作

    //
    Long countByAuthor(User user);

    
    // 根据作者查找文章
    // 使用 JOIN FETCH 来预加载 author 关联
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.author = :user ORDER BY p.createdAt DESC")
    List<Post> findByAuthorOrderByCreatedAtDesc(User user);

    
    // 查找用户点赞过的文章（分页版）
    // 使用 JOIN 和 WHERE 子句来查询用户点赞过的文章
    @Query("""
        SELECT p FROM Post p
        JOIN Like l ON l.post = p
        WHERE l.user.id = :userId
        ORDER BY l.likedAt DESC
        """)
    Page<Post> findLikedPostsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // 根据作者查找文章（分页版）
    // 使用 JOIN FETCH 来预加载 author 关联
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.author = :user ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorOrderByCreatedAtDesc(User user, Pageable pageable);

    //根据权限查文章 
    // 使用 JOIN FETCH 来预加载 author 关联
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.visibility = :visibility ORDER BY p.createdAt DESC")
    List<Post> findByVisibilityOrderByCreatedAtDesc(String visibility);

    //查指定作者+可见性 - 预加载author关联
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.author = :user AND p.visibility = :visibility ORDER BY p.createdAt DESC")
    List<Post> findByAuthorAndVisibilityOrderByCreatedAtDesc(User user, String visibility);


    /**
     * 根据作者和多个可见性状态查询文章（分页版）- 预加载author关联
     * @param user 作者
     * @param visibilities 可见性状态列表
     * @param pageable 分页参数
     * @return 分页后的文章列表
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.author = :user AND p.visibility IN :visibilities ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorAndVisibilityInOrderByCreatedAtDesc(User user, List<String> visibilities, Pageable pageable);



}
