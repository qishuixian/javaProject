package learning.boot;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// 参数接收实验：回显绑定结果，不写入数据库。
@RestController
@RequestMapping("/api/day34")
public class Day34BindingController {
    @GetMapping("/teachers/{id}")
    public Map<String, Object> teacher(@PathVariable("id") long id) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("source", "path");
        result.put("teacherId", id);
        return result;
    }

    @GetMapping("/teachers")
    public Map<String, Object> search(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        // 参数绑定只负责转换；页码是否合法需要另行判断。
        if (page < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 1");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("source", "query");
        result.put("keyword", keyword);
        result.put("page", page);
        return result;
    }

    @PostMapping("/teachers/preview")
    public Map<String, Object> preview(@RequestBody TeacherPreviewRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("source", "body");
        result.put("name", request.getName());
        result.put("age", request.getAge());
        result.put("saved", false);
        return result;
    }
}
