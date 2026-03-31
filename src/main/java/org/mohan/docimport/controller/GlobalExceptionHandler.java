package org.mohan.docimport.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.mohan.docimport.common.CommonResult;
import org.mohan.docimport.exception.DocumentImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器，统一处理上传大小限制和未捕获异常。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.servlet.multipart.max-file-size:100MB}")
    private String maxFileSize;

    /**
     * 处理文件上传超限异常。
     *
     * @param exception 超限异常
     * @param request 当前请求
     * @return 413 响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonResult<Void>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        log.warn("Upload rejected because file size exceeded limit. uri={}, limit={}",
                request.getRequestURI(), maxFileSize, exception);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(CommonResult.error(413, "上传文件过大，当前单文件上限为 " + maxFileSize));
    }

    /**
     * 处理文档导入业务异常。
     *
     * @param exception 业务异常
     * @param request 当前请求
     * @return 对应状态码响应
     */
    @ExceptionHandler(DocumentImportException.class)
    public ResponseEntity<CommonResult<Void>> handleDocumentImportException(
            DocumentImportException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = resolveStatus(exception.getCode());
        log.warn("Document import failed. uri={}, code={}, message={}",
                request.getRequestURI(), exception.getCode(), exception.getMessage(), exception);
        return ResponseEntity.status(status)
                .body(CommonResult.error(exception.getCode(), exception.getMessage()));
    }

    /**
     * 处理其他未捕获异常。
     *
     * @param exception 异常对象
     * @param request 当前请求
     * @return 500 响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResult<Void>> handleException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception. uri={}", request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResult.error(500, "服务器内部错误"));
    }

    private HttpStatus resolveStatus(int code) {
        return switch (code) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 413 -> HttpStatus.PAYLOAD_TOO_LARGE;
            case 415 -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            case 422 -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
