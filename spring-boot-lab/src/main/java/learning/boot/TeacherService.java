package learning.boot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {
    private final String teacherName;

    // Spring 读取配置，再把配置值传入构造方法。
    public TeacherService(@Value("${lesson.teacher-name}") String teacherName) {
        this.teacherName = teacherName;
    }

    public String findTeacherName() {
        return teacherName;
    }
}
