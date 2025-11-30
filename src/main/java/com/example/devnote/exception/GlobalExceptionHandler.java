package com.example.devnote.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理类（Global Exception Handler）
 *
 * 作用：
 *  - 捕获全局 Controller 层的异常
 *  - 显示统一的错误页面（error.html）
 *  - 避免用户看到堆栈信息，提升用户体验
 *
 * @ControllerAdvice 是 Spring 提供的全局异常管理机制
 * 能让我们集中处理所有控制器（Controller）的错误
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /**
     * 处理 404 页面未找到异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex, Model model) {
        logger.warn("页面未找到: {}", ex.getRequestURL());
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorMessage", "页面未找到：" + ex.getRequestURL());
        return "error"; // 返回 templates/error.html
    }

    /**
     * 处理所有其他异常（500）
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralError(Exception ex, Model model) {
        logger.error("服务器异常: ", ex);
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorMessage", "服务器内部错误：" + ex.getMessage());
        return "error";
    }
}
