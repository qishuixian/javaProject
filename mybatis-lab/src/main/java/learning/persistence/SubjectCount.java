package learning.persistence;

// 聚合查询的结果对象，不是 teacher 表的一行。
public class SubjectCount {
    private String subject;
    private Long teacherCount;
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public Long getTeacherCount() { return teacherCount; }
    public void setTeacherCount(Long teacherCount) { this.teacherCount = teacherCount; }
}
