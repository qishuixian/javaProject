package learning.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface Day48BatchMapper {
    List<LessonRow> lessons(@Param("maxId") long maxId);
    TeacherRow teacher(@Param("id") long id);
    List<TeacherRow> teachers(@Param("ids") List<Long> ids);
}
