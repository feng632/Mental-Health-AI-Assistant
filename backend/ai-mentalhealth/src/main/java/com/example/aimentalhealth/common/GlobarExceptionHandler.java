package com.example.aimentalhealth.common;

import com.example.aimentalhealth.exception.BusinessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobarExceptionHandler {
    //处理参数异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handlerException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(ResultCode.PARAM_ERROR.getCode(),ResultCode.PARAM_ERROR.getMsg(), message);
    }

    //处理业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> handlerBusinessException(BusinessException e) {
        //如果异常携带额外数据
        if (e.getData() != null) {
            return Result.error(e.getCode(), e.getMessage(), e.getData());
        }

        //如果没有携带额外数据
        return Result.error(e.getCode(), e.getMessage(),null);


    }

    //处理请求体格式错误（JSON 解析失败等）
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handlerNotReadable(HttpMessageNotReadableException e) {
        return Result.error(ResultCode.PARAM_ERROR.getCode(), "请求体格式错误", e.getMessage());
    }

    //兜底：其它未处理的异常
    @ExceptionHandler(Exception.class)
    public Result<?> handlerUnknownException(Exception e) {
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMsg(), e.getMessage());
    }
}
