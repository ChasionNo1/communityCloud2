package com.chasion.exce;

import com.chasion.resp.ResultData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserServiceExceptionHandler {

    // 处理 UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResultData<?>> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResultData.fail(e.getCode(), e.getMessage()));
    }

    // 其他异常处理（如参数校验、全局异常等）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultData<?>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResultData.fail("400", errorMsg));
    }
}
