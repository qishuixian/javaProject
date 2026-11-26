package learning.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface Day44TeacherMapper {
    long count(@Param("subject") String subject);
    long countAll();
    List<TeacherRow> page(@Param("subject") String subject,
            @Param("limit") int limit, @Param("offset") long offset);
}
