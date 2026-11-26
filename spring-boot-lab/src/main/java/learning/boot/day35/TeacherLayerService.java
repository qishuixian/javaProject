package learning.boot.day35;

import org.springframework.stereotype.Service;

@Service
public class TeacherLayerService {
    public TeacherResponse example() {
        TeacherEntity entity = new TeacherEntity(10001L, "张老师", "Java", "仅供内部教学演示");
        return TeacherConverter.toResponse(entity);
    }

    public TeacherResponse preview(TeacherCreateRequest request) {
        // 没有保存，因此 id 为 null。内部字段由服务端决定。
        TeacherEntity entity = new TeacherEntity(null, request.getName(),
                request.getSubject(), "待内部审核");
        return TeacherConverter.toResponse(entity);
    }
}
