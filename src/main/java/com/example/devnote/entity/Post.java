package com.example.devnote.entity;

import jakarta.persistence.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Entity 表示这个类是一个 JPA 实体，会映射到数据库的一张表
 * @Table(name = "posts") 指定数据库表名为 `posts`
 * 如果不写 @Table，默认表名是类名小写：post
 */
@Entity
@Table(name = "posts")
public class Post {
    /**
     * @Id 表示这是主键
     * 数据库中会作为唯一标识，比如 1, 2, 3...
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     * 表示主键由数据库自动增长（auto_increment）
     * 比如 MySQL 的 AUTO_INCREMENT
     * 插入数据时，不用手动设置 id，数据库会自动分配
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column(nullable = false)
     * 表示这是一个数据库字段，且不允许为 NULL
     * 生成的 SQL 会是：title VARCHAR(255) NOT NULL
     */
    @NotEmpty(message = "标题不为空")
    @Size(max =255,message = "标题长度不能超过255个字符")
    @Column(nullable = false)
    private String title;

    /**
     * @Lob 表示“大对象”（Large Object）
     * 适合存储长文本（如文章内容）
     * columnDefinition = "TEXT" 指定数据库类型为 TEXT（而不是默认的 VARCHAR(255)）
     */
    @NotEmpty(message = "内容不能为空")
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 文章创建时间
     * updatable = false 表示更新时不会修改这个字段
     * 比如执行 update 时，createdAt 不会被更新
     */
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 多篇文章对应一个用户
     * ManyToOne 表示“多对一”关系
     * fetch = FetchType.LAZY 可以提升性能（延迟加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")//外键列名
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    //权限控制字段
    @Column(nullable = false)
    private String visibility = "PUBLIC";




    public Post() {
        // JPA 要求实体类必须有一个无参构造函数
    }

    // ================== Getter 和 Setter ==================
    // 注意：JPA 通过 setter 方法设置字段值，不能删！
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
