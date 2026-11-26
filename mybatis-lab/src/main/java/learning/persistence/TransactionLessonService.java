package learning.persistence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionLessonService {
    private final Day42TeacherMapper mapper;
    public TransactionLessonService(Day42TeacherMapper mapper) { this.mapper = mapper; }

    private void insert(String name) {
        TeacherRow row = new TeacherRow();
        row.setName(name); row.setSubject("事务实验");
        mapper.insert(row);
    }

    @Transactional
    public void succeed(String name) { insert(name); }

    @Transactional
    public void runtimeFailure(String name) {
        insert(name);
        throw new IllegalStateException("模拟运行时异常");
    }

    // 外部调用此方法时没有事务；this 调用不经过事务代理。
    public void selfCall(String name) { this.runtimeFailure(name); }

    @Transactional
    public void caughtInside(String name) {
        insert(name);
        try {
            throw new IllegalStateException("在事务方法内部捕获");
        } catch (IllegalStateException expected) {
            // 仅作反例：方法正常返回，代理没有看到异常，也没有 rollback-only 标记。
        }
    }

    @Transactional
    public void checkedFailure(String name) throws Exception {
        insert(name);
        throw new Exception("模拟受检异常");
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkedRollback(String name) throws Exception {
        insert(name);
        throw new Exception("显式配置受检异常回滚");
    }
}
