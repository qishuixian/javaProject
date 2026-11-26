package learning.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface Day43TeacherMapper {
    List<TeacherRow> search(@Param("name") String name, @Param("subject") String subject,
            @Param("minId") Long minId, @Param("sort") String sort, @Param("direction") String direction);
}
