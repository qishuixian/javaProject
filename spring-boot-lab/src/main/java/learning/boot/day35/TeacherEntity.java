package learning.boot.day35;

// 模拟持久化实体；本课没有 ORM 注解，也不连接数据库。
public class TeacherEntity {
    private final Long id;
    private final String name;
    private final String subject;
    private final String internalRemark;

    public TeacherEntity(Long id, String name, String subject, String internalRemark) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.internalRemark = internalRemark;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSubject() { return subject; }
    public String getInternalRemark() { return internalRemark; }
}
