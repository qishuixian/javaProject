package learning.boot.day37;

import java.util.List;
import java.util.Map;

// 错误响应 DTO：不包含异常对象、堆栈或原始请求体。
public class ApiError {
    private final String code;
    private final String message;
    private final String requestId;
    private final List<Map<String, String>> errors;

    public ApiError(String code, String message, String requestId, List<Map<String, String>> errors) {
        this.code = code;
        this.message = message;
        this.requestId = requestId;
        this.errors = errors;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getRequestId() { return requestId; }
    public List<Map<String, String>> getErrors() { return errors; }
}
