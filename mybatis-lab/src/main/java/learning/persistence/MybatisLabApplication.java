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
            TeacherSearchService search = context.getBean(TeacherSearchService.class);
            System.out.println("Day43 无条件数量：" + search.search(null, null, null, null, null).size());
            for (TeacherRow teacher : search.search(null, "Java", 2L, "id", "desc")) {
                System.out.println("Day43 Java且编号>=2：" + teacher.getId() + " / " + teacher.getName());
            }
            try {
                search.search(null, null, null, "id; DROP TABLE teacher", "asc");
                throw new IllegalStateException("非法排序没有被拒绝");
            } catch (IllegalArgumentException expected) {
                System.out.println("Day43 非法排序已拒绝：" + expected.getMessage());
            }
        }
    }
}
