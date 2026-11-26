package learning.persistence;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

// 通用单表 CRUD 来自 BaseMapper；聚合查询使用明确的自定义 SQL。
public interface Day42TeacherMapper extends BaseMapper<TeacherRow> {
    List<SubjectCount> countBySubject();
}
