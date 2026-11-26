package learning.day32;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class Day32Injection {
    public interface TeacherRepository {
        String findName(long id);
    }

    public static class MemoryTeacherRepository implements TeacherRepository {
        public String findName(long id) {
            return id == 10001L ? "张老师" : null;
        }
    }

    public static class TeacherService {
        private final TeacherRepository repository;

        // 不在业务类里 new 具体仓库，由外部通过构造方法提供。
        public TeacherService(TeacherRepository repository) {
            if (repository == null) {
                throw new IllegalArgumentException("仓库不能为 null");
            }
            this.repository = repository;
        }

        public String teacherLabel(long id) {
            String name = repository.findName(id);
            return name == null ? "教师不存在" : "教师：" + name;
        }
    }

    @Configuration
    public static class AppConfig {
        @Bean
        public TeacherRepository teacherRepository() {
            return new MemoryTeacherRepository();
        }

        // Spring 自动寻找 TeacherRepository Bean，并作为参数传进来。
        @Bean
        public TeacherService teacherService(TeacherRepository repository) {
            return new TeacherService(repository);
        }
    }

    public static void main(String[] args) {
        TeacherService manual = new TeacherService(new MemoryTeacherRepository());
        System.out.println("手动注入：" + manual.teacherLabel(10001L));

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {
            TeacherService service = context.getBean(TeacherService.class);
            System.out.println("Spring 注入：" + service.teacherLabel(10001L));
            System.out.println("查询无结果：" + service.teacherLabel(99999L));
            System.out.println("默认共享同一个 Bean：" +
                    (service == context.getBean(TeacherService.class)));

            // 用一个假实现替换仓库，不修改业务类。
            TeacherRepository fake = id -> "测试老师";
            TeacherService testService = new TeacherService(fake);
            System.out.println("替换仓库：" + testService.teacherLabel(10001L));
            if (!"教师：张老师".equals(service.teacherLabel(10001L))
                    || !"教师不存在".equals(service.teacherLabel(99999L))
                    || !"教师：测试老师".equals(testService.teacherLabel(10001L))) {
                throw new IllegalStateException("注入验证失败");
            }
        }
        System.out.println("Spring 容器已关闭");
    }
}
