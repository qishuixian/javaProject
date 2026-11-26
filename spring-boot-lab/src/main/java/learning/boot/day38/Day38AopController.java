package learning.boot.day38;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.aop.support.AopUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/day38")
public class Day38AopController {
    private final TimedTeacherService teacherService;
    private final TeacherCallerService callerService;

    public Day38AopController(TimedTeacherService teacherService, TeacherCallerService callerService) {
        this.teacherService = teacherService;
        this.callerService = callerService;
    }

    @GetMapping("/direct")
    public Map<String, Object> direct() { return result("direct", teacherService.findName()); }

    @GetMapping("/self")
    public Map<String, Object> self() { return result("self", teacherService.selfCall()); }

    @GetMapping("/cross")
    public Map<String, Object> cross() { return result("cross", callerService.crossCall()); }

    @GetMapping("/manual")
    public Map<String, Object> manual() {
        // 教学对照：手动 new 的对象没有 Spring 代理。
        return result("manual", new TimedTeacherService().findName());
    }

    private Map<String, Object> result(String mode, String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mode", mode);
        result.put("teacherName", name);
        // 该字段描述注入的对象；manual 模式实际调用的是另一个手动创建的对象。
        result.put("injectedServiceIsProxy", AopUtils.isAopProxy(teacherService));
        return result;
    }
}
