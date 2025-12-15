package com.example.devnote.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * 处理校验失败异常，返回 JSON 格式错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        

        // 创建一个 Map 来存储字段名和错误信息
        Map<String, String> errors = new HashMap<>();

        // 遍历所有校验错误，将字段名和错误信息添加到 Map 中
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String msg = error.getDefaultMessage();
            errors.put(field, msg);
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
