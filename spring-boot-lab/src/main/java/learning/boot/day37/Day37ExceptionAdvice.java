package learning.boot.day37;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.WebRequest;

// 只统一 Day37 的错误；历史课程的响应格式不受影响。
@RestControllerAdvice(assignableTypes = Day37ExceptionController.class)
public class Day37ExceptionAdvice extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(Day37ExceptionAdvice.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("field", error.getField());
            item.put("message", error.getDefaultMessage());
            errors.add(item);
        }
        return failure(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "参数校验失败", errors, null);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        return failure(HttpStatus.BAD_REQUEST, "INVALID_BODY", "请求体缺失、JSON格式错误或字段类型不匹配", Collections.emptyList(), null);
    }

    @ExceptionHandler(TeacherNotFoundException.class)
    public ResponseEntity<Object> business(TeacherNotFoundException ex) {
        return failure(HttpStatus.NOT_FOUND, "TEACHER_NOT_FOUND", "教师不存在", Collections.emptyList(), null);
    }

    // 保留框架自身的 HTTP 状态，避免把类型错误等全部包装为 500。
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        String code = ex instanceof MethodArgumentTypeMismatchException ? "INVALID_PARAMETER" : "HTTP_ERROR";
        ResponseEntity<Object> response = failure(status, code, "请求无法处理，请检查请求方式与参数", Collections.emptyList(),
                status.is5xxServerError() ? ex : null);
        return new ResponseEntity<>(response.getBody(), headers, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unexpected(Exception ex) {
        return failure(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "服务暂时不可用，请稍后重试", Collections.emptyList(), ex);
    }

    private ResponseEntity<Object> failure(HttpStatus status, String code, String message,
            List<Map<String, String>> errors, Exception unexpected) {
        String requestId = UUID.randomUUID().toString();
        if (unexpected != null) {
            // Throwable 作为最后一个参数传入，记录完整堆栈。
            LOG.error("requestId=" + requestId + " code=" + code, unexpected);
        } else {
            LOG.warn("requestId={} code={}", requestId, code);
        }
        return ResponseEntity.status(status).body(new ApiError(code, message, requestId, errors));
    }
}
