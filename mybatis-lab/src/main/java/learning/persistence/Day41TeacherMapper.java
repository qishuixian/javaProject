package learning.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;

// 普通 MyBatis 接口：SQL 位于同名 XML。
public interface Day41TeacherMapper {
    TeacherRow findById(@Param("id") long id);
    List<TeacherRow> findBySubject(@Param("subject") String subject);
}
