package com.example.devnote.exception;

// 用户已存在异常
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String username) {
        super("用户已存在: " + username);
    }
    
}
