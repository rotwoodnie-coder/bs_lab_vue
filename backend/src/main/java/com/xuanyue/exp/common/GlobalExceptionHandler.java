package com.xuanyue.exp.common;

import com.xuanyue.exp.common.ApiResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String method = ex.getMethod() == null ? "UNKNOWN" : ex.getMethod();
        return ApiResponse.fail(405, "请求方法 '" + method + "' 不支持，请使用 " + ex.getSupportedHttpMethods());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ApiResponse.fail(413, "上传文件超过服务器大小限制");
    }

    @ExceptionHandler(MultipartException.class)
    public ApiResponse<Void> handleMultipartException(MultipartException ex) {
        return ApiResponse.fail(400, "上传请求无效：" + rootMessage(ex));
    }

    @ExceptionHandler(ClientAbortException.class)
    public ApiResponse<Void> handleClientAbort(ClientAbortException ex) {
        return ApiResponse.fail(499, "客户端已取消上传");
    }

    @ExceptionHandler(IOException.class)
    public ApiResponse<Void> handleIOException(IOException ex) {
        return ApiResponse.fail(500, "文件读写失败：" + rootMessage(ex));
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntimeException(RuntimeException ex) {
        return ApiResponse.fail(500, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        return ApiResponse.fail(500, ex.getMessage() == null ? "服务器内部错误" : ex.getMessage());
    }

    private String rootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() == null ? ex.getMessage() : root.getMessage();
    }
}
