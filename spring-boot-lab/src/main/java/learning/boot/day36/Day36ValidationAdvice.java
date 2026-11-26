package learning.boot.day36;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 仅处理 Day36 Controller 的输入错误；完整异常体系留到 Day37。
@RestControllerAdvice(assignableTypes = Day36ValidationController.class)
public class Day36ValidationAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validation(MethodArgumentNotValidException exception) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("field", error.getField());
            item.put("message", error.getDefaultMessage());
            errors.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", "VALIDATION_ERROR");
        result.put("message", "参数校验失败");
        result.put("errors", errors);
        return result;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> unreadable() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", "INVALID_BODY");
        result.put("message", "请求体缺失、JSON格式错误或字段类型不匹配");
        return result;
    }
}
