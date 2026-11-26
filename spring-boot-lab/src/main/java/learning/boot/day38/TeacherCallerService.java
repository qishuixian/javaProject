package learning.boot.day38;

import org.springframework.stereotype.Service;

@Service
public class TeacherCallerService {
    private final TimedTeacherService teacherService;

    public TeacherCallerService(TimedTeacherService teacherService) {
        this.teacherService = teacherService;
    }

    public String crossCall() {
        // 注入的是 Spring 管理的代理，调用会经过切面。
        return teacherService.findName();
    }
}
