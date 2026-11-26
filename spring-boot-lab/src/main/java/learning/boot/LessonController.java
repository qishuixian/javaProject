package learning.boot;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LessonController {
    private final TeacherService teacherService;
    private final String environment;

    // 单个构造方法不必写 @Autowired：Spring 自动提供 Service Bean。
    public LessonController(TeacherService teacherService,
                            @Value("${lesson.environment}") String environment) {
        this.teacherService = teacherService;
        this.environment = environment;
    }

    @GetMapping("/api/lesson")
    public Map<String, Object> lesson() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("day", 33);
        result.put("environment", environment);
        result.put("teacherName", teacherService.findTeacherName());
        return result;
    }
}
