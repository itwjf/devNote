package com.example.devnote.service;

import com.example.devnote.entity.Comment;
import com.example.devnote.entity.Post;
import com.example.devnote.entity.User;
import com.example.devnote.repository.CommentRepository;
import com.example.devnote.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * CommentService
 *
 * 负责处理评论相关的业务逻辑：
 *  - 添加评论或子评论
 *  - 查询文章下的所有评论（包含层级）
 *  - 删除评论（仅作者或文章作者有权限）
 */
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    /**
     * 根据文章查询所有顶级评论
     */
    public List<Comment> getCommentsByPost(Post post) {
        return commentRepository.findByPostAndParentIsNullOrderByCreatedAtAsc(post);
    }

    /**
     * 添加评论或回复
     */
    public Comment addComment(Long postId, String content, User author, Long parentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setPost(post);

        // 如果是回复某条评论
        if (parentId != null) {
            Optional<Comment> parentComment = commentRepository.findById(parentId);
            parentComment.ifPresent(comment::setParent);
        }

        return commentRepository.save(comment);
    }

    /**
     * 删除评论（仅作者或文章作者有权删除）
     */
    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        Post post = comment.getPost();
        User postAuthor = post.getAuthor();

        // 权限验证
        if (!comment.getAuthor().getId().equals(currentUser.getId()) &&
                !postAuthor.getId().equals(currentUser.getId())) {
            throw new RuntimeException("你没有权限删除此评论");
        }

        // 删除子评论
        commentRepository.deleteAll(comment.getReplies());
        //删除评论本身
        commentRepository.delete(comment);
    }
}
