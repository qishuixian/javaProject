package learning.persistence;

import java.sql.Connection;
import java.util.*;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:day48;DB_CLOSE_DELAY=-1")
@Import(Day48BatchTest.CounterConfig.class)
class Day48BatchTest {
    @Autowired private LessonTeacherService service;
    @Autowired private Day48BatchMapper mapper;
    @Autowired private SqlCounter counter;

    @Test void batchKeepsOrderDuplicatesAndMissingTeacher() {
        counter.reset();
        List<String> naive = service.naive(5);
        int naiveCount = counter.count();
        counter.reset();
        List<String> batch = service.batch(5);
        int batchCount = counter.count();
        assertEquals(Arrays.asList("1:李老师", "2:张老师", "3:李老师", "4:教师不存在", "5:王老师"), batch);
        assertEquals(naive, batch);
        assertEquals(6, naiveCount); assertEquals(2, batchCount);
        System.out.println("Day48 SQL次数：" + naiveCount + " -> " + batchCount + "，结果=" + batch);
    }
    @Test void emptyLessonsOnlyExecuteInitialQuery() {
        counter.reset(); assertTrue(service.batch(0).isEmpty()); assertEquals(1, counter.count());
        counter.reset(); assertTrue(service.naive(0).isEmpty()); assertEquals(1, counter.count());
    }
    @Test void emptyIdsNeverReturnAllTeachers() {
        assertTrue(mapper.teachers(Collections.emptyList()).isEmpty());
        assertTrue(mapper.teachers(null).isEmpty());
    }

    @TestConfiguration
    static class CounterConfig {
        @Bean SqlCounter sqlCounter() { return new SqlCounter(); }
    }
    // 统计本测试线程到达 JDBC prepare 的语句，不把 Mapper 调用次数当作 SQL 次数。
    @Intercepts(@Signature(type = StatementHandler.class, method = "prepare",
            args = {Connection.class, Integer.class}))
    public static class SqlCounter implements Interceptor {
        private final ThreadLocal<Integer> count = ThreadLocal.withInitial(() -> 0);
        public void reset() { count.set(0); }
        public int count() { return count.get(); }
        @Override public Object intercept(Invocation invocation) throws Throwable {
            count.set(count.get() + 1);
            return invocation.proceed();
        }
        @Override public Object plugin(Object target) { return Plugin.wrap(target, this); }
    }
}
