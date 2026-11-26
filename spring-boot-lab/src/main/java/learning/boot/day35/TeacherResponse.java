package learning.boot.day35;

// 响应 DTO：只定义对外公开的字段，没有内部备注。
public class TeacherResponse {
    private final Long id;
    private final String name;
    private final String subject;

    public TeacherResponse(Long id, String name, String subject) {
        this.id = id;
        this.name = name;
        this.subject = subject;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSubject() { return subject; }
}
