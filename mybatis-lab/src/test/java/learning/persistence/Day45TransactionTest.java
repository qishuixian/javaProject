package learning.persistence;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
// 不加测试级 @Transactional：否则外层测试事务会掩盖 selfCall 的真实表现。
class Day45TransactionTest {
    @Autowired private TransactionLessonService service;
    @Autowired private Day42TeacherMapper mapper;

    private long count(String name) {
        return mapper.selectCount(new QueryWrapper<TeacherRow>().eq("teacher_name", name));
    }
    private void cleanup(String name) {
        mapper.delete(new QueryWrapper<TeacherRow>().eq("teacher_name", name));
    }
    @Test void normalReturnCommits() {
        String name = "day45-success";
        try { service.succeed(name); assertEquals(1, count(name)); }
        finally { cleanup(name); }
    }
    @Test void runtimeExceptionEscapingProxyRollsBack() {
        String name = "day45-runtime";
        try {
            assertThrows(IllegalStateException.class, () -> service.runtimeFailure(name));
            assertEquals(0, count(name));
        } finally { cleanup(name); }
    }
    @Test void selfInvocationDoesNotStartTransaction() {
        String name = "day45-self";
        try {
            assertThrows(IllegalStateException.class, () -> service.selfCall(name));
            assertEquals(1, count(name));
        } finally { cleanup(name); }
    }
    @Test void caughtLocalExceptionAllowsCommit() {
        String name = "day45-caught";
        try { service.caughtInside(name); assertEquals(1, count(name)); }
        finally { cleanup(name); }
    }
    @Test void checkedExceptionCommitsByDefault() {
        String name = "day45-checked";
        try {
            assertThrows(Exception.class, () -> service.checkedFailure(name));
            assertEquals(1, count(name));
        } finally { cleanup(name); }
    }
    @Test void rollbackForIncludesCheckedException() {
        String name = "day45-configured";
        try {
            assertThrows(Exception.class, () -> service.checkedRollback(name));
            assertEquals(0, count(name));
        } finally { cleanup(name); }
    }
}
