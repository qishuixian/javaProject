package learning.persistence;

import com.baomidou.mybatisplus.annotation.*;

@TableName("teacher")
public class TeacherRow {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("teacher_name")
    private String name;
    private String subject;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}
