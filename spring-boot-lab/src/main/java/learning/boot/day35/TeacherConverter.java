package learning.boot.day35;

public class TeacherConverter {
    private TeacherConverter() { }

    // 显式选择返回字段；不要把整个 Entity 直接返回给 Controller。
    public static TeacherResponse toResponse(TeacherEntity entity) {
        return new TeacherResponse(entity.getId(), entity.getName(), entity.getSubject());
    }
}
