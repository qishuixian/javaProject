package learning.boot.day40;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import learning.boot.day35.TeacherEntity;
import org.springframework.stereotype.Repository;

// 进程内存仓库：重启即丢失，不替代真实数据库。
@Repository
public class MemoryTeacherRepository {
    private final AtomicLong sequence = new AtomicLong(10000);
    private final ConcurrentHashMap<Long, TeacherEntity> teachers = new ConcurrentHashMap<>();

    public TeacherEntity create(String name, String subject) {
        long id = sequence.incrementAndGet();
        TeacherEntity entity = new TeacherEntity(id, name, subject, "内部创建记录");
        teachers.put(id, entity);
        return entity;
    }

    public TeacherEntity find(long id) { return teachers.get(id); }

    public TeacherEntity update(long id, String name, String subject) {
        // 不存在时不创建；用新的不可变对象替换，避免字段更新到一半被读到。
        return teachers.computeIfPresent(id, (key, old) ->
                new TeacherEntity(key, name, subject, old.getInternalRemark()));
    }
}
