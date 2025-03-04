package com.sellect.server.common.exception.handler;

import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.util.ErrorCode;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.BindException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * javax.validation.Valid or @Validated 으로 binding error 발생시 발생한다.
     * HttpMessageConverter 에서 등록한 HttpMessageConverter binding 못할 경우 발생
     * 주로 @RequestBody, @RequestPart 어노테이션에서 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponse<Void> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e, HttpServletRequest servletResponse) {
        log.error("handleMethodArgumentNotValidException {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE,
            e.getBindingResult());
        return ApiResponse.onFailure(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * @ModelAttribute 으로 binding error 발생시 BindException 발생한다.
     */
    @ExceptionHandler(BindException.class)
    protected ApiResponse<Void> handleBindException(
        BindException e, HttpServletResponse servletResponse) {
        log.error("handleBindException {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE);
        servletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return ApiResponse.onFailure(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * @PathVariable 에서 validation을 할 때 binding error가 발생하는 경우
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ApiResponse<Void> handleConstraintViolationException(
        ConstraintViolationException e, HttpServletResponse servletResponse) {
        log.error("handleConstraintViolationException {}", e.getMessage());
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, constraintViolations);
        servletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return ApiResponse.onFailure(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * servlet request parameter binding error 가 발생하는 경우
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    protected ApiResponse<Void> handleServletRequestBindingException(
        ServletRequestBindingException e, HttpServletResponse servletResponse) {
        log.error("handleServletRequestBindingException {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.BAD_REQUEST);
        servletResponse.setStatus(response.getStatus());
        return ApiResponse.onFailure(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 주로 @RequestParam 에서 enum 으로 binding 못할 경우 발생
     *
     * @PathVariable에서 string -> int/long 등의 숫자 타입 binding 못할 경우
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ApiResponse<Void> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e, HttpServletResponse servletResponse) {
        log.error("handleMethodArgumentTypeMismatchException {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(e);
        servletResponse.setStatus(response.getStatus());
        return ApiResponse.onFailure(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * body to dto 과정에서 enum 형태로 deserialize 하지 못하는 경우 발생 또한,
     * 숫자가 int 혹은 long type 등의 범위를 벗어나는 경우 발생
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ApiResponse<Void> handleHttpMessageNotReadableExceptionException(
        HttpMessageNotReadableException e, HttpServletResponse servletResponse) {
        log.error("handleHttpMessageNotReadableExceptionException {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(e);
        servletResponse.setStatus(response.getStatus());
        return ApiResponse.onFailure(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 지원하지 않은 HTTP method 호출 할 경우 발생
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ApiResponse<Void> handleHttpRequestMethodNotSupportedException(
        HttpRequestMethodNotSupportedException e, HttpServletResponse servletResponse) {
        log.error("handleHttpRequestMethodNotSupportedException {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);
        servletResponse.setStatus(response.getStatus());
        return ApiResponse.onFailure(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(CommonException.class)
    public ApiResponse<Void> handleCommonException(
        final CommonException e, HttpServletResponse servletResponse) {
        log.error("CommonException {}", e.getMessage());
        final ErrorResponse errorResponse = ErrorResponse.of(e);
        servletResponse.setStatus(errorResponse.getStatus());
        return ApiResponse.onFailure(errorResponse, HttpStatus.valueOf(errorResponse.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    protected ApiResponse<Void> handleUnExpectException(
        Exception e, HttpServletResponse servletResponse) {
        log.error("UnExpectException {}", e.getMessage());
        final ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        servletResponse.setStatus(response.getStatus());
        return ApiResponse.onFailure(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}