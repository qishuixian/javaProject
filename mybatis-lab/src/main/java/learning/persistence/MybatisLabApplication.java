package learning.persistence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("learning.persistence")
public class MybatisLabApplication {
    public static void main(String[] args) {
        // 控制台实验：执行完关闭连接池，无 HTTP 端口。
        try (ConfigurableApplicationContext context = SpringApplication.run(MybatisLabApplication.class, args)) {
            Day41TeacherMapper xml = context.getBean(Day41TeacherMapper.class);
            System.out.println("Day41 编号1姓名：" + xml.findById(1L).getName());
            System.out.println("Day41 Java教师数：" + xml.findBySubject("Java").size());
            Day42TeacherMapper plus = context.getBean(Day42TeacherMapper.class);
            TeacherRow row = new TeacherRow();
            row.setName("赵老师"); row.setSubject("英语");
            plus.insert(row);
            System.out.println("Day42 新增编号：" + row.getId());
            row.setName("赵老师（更新）");
            plus.updateById(row);
            System.out.println("Day42 更新后：" + plus.selectById(row.getId()).getName());
            for (SubjectCount count : plus.countBySubject()) {
                System.out.println("Day42 科目统计：" + count.getSubject() + "=" + count.getTeacherCount());
            }
            plus.deleteById(row.getId());
            System.out.println("Day42 删除后为空：" + (plus.selectById(row.getId()) == null));
        }
    }
}
