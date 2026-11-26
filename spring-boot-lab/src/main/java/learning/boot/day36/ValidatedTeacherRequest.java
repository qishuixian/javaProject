package learning.boot.day36;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ValidatedTeacherRequest {
    @NotBlank(message = "姓名不能为空或全是空格")
    @Size(max = 20, message = "姓名长度不能超过20")
    private String name;

    @NotBlank(message = "科目不能为空或全是空格")
    @Size(max = 30, message = "科目长度不能超过30")
    private String subject;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}
