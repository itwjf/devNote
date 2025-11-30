package com.example.devnote.repository;

import com.example.devnote.entity.Comment;
import com.example.devnote.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 评论仓库接口
 *
 * 提供对评论表的基础 CRUD 操作。
 * 继承 JpaRepository 后，自动具备：
 *   - save()：保存评论
 *   - findById()：按 ID 查找评论
 *   - delete()：删除评论
 *   - findAll()：查询所有评论
 */
public interface CommentRepository extends JpaRepository<Comment,Long> {

    /**
     * 根据文章查询所有顶级评论（parent 为空）
     * 并按时间顺序排序。
     */
    List<Comment> findByPostAndParentIsNullOrderByCreatedAtAsc(Post post);


    /**
     * 根据父评论查找所有子评论。
     */
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);

}
