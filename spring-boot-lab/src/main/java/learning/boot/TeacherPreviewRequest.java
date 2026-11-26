package learning.boot;

// 接收 JSON 的普通 Java 对象，不是数据库实体。
public class TeacherPreviewRequest {
    private String name;
    // Integer 可以用 null 表示没传，int 则默认是 0。
    private Integer age;

    public TeacherPreviewRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
