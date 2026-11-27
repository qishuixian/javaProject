package learning.persistence;

import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class LessonTeacherService {
    private final Day48BatchMapper mapper;
    public LessonTeacherService(Day48BatchMapper mapper) { this.mapper = mapper; }

    // 教学对照：先查课节，再循环查询每个教师。
    public List<String> naive(long maxId) {
        List<String> result = new ArrayList<>();
        for (LessonRow lesson : mapper.lessons(maxId)) {
            TeacherRow teacher = mapper.teacher(lesson.getTeacherId());
            result.add(label(lesson, teacher));
        }
        return result;
    }

    public List<String> batch(long maxId) {
        List<LessonRow> lessons = mapper.lessons(maxId);
        if (lessons.isEmpty()) { return Collections.emptyList(); }
        Set<Long> uniqueIds = new LinkedHashSet<>();
        for (LessonRow lesson : lessons) { uniqueIds.add(lesson.getTeacherId()); }
        List<TeacherRow> teachers = mapper.teachers(new ArrayList<>(uniqueIds));
        Map<Long, TeacherRow> byId = new HashMap<>();
        for (TeacherRow teacher : teachers) { byId.put(teacher.getId(), teacher); }
        List<String> result = new ArrayList<>();
        // 按原始课节顺序组装，不依赖 IN 查询的返回顺序。
        for (LessonRow lesson : lessons) { result.add(label(lesson, byId.get(lesson.getTeacherId()))); }
        return result;
    }

    private String label(LessonRow lesson, TeacherRow teacher) {
        // 字符串仅用于控制台教学，正式接口应定义响应 DTO。
        return lesson.getId() + ":" + (teacher == null ? "教师不存在" : teacher.getName());
    }
}
