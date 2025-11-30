package com.example.devnote.repository;

import com.example.devnote.controller.LikeController;
import com.example.devnote.entity.Follow;
import com.example.devnote.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow,Long> {
    // 检查是否已关注
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    // 获取某个用户的所有粉丝
    List<Follow> findByFollowing(User user);


    // 获取某个用户关注的所有人
    List<Follow> findByFollower(User user);

    // 查询我关注的人（返回 User）
    @Query("SELECT f.following FROM Follow f WHERE f.follower = :user")
    List<User> findFollowingUsers(@Param("user") User user);

    // 查询关注我的人（返回 User）
    @Query("SELECT f.follower FROM Follow f WHERE f.following = :user")
    List<User> findFollowers(@Param("user") User user);

    /**
     * 分页查询某用户的粉丝（返回 User 列表）
     */
    @Query("SELECT f.follower FROM Follow f WHERE f.following = :user")
    Page<User> findFollowersPage(@Param("user") User user, Pageable pageable);
    /**
     * 分页查询某用户的关注（返回 User 列表）
     */
    @Query("SELECT f.following FROM Follow f WHERE f.follower = :user")
    Page<User> findFollowingPage(@Param("user") User user, Pageable pageable);



    // 取消关注
    void deleteByFollowerAndFollowing(User follower, User following);

    // 统计
    long countByFollower(User follower);
    long countByFollowing(User following);



}
