package learning.boot.day35;

// 请求 DTO：只接收允许客户端填写的字段。
public class TeacherCreateRequest {
    private String name;
    private String subject;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}
