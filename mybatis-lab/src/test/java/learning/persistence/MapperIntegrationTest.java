package learning.persistence;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MapperIntegrationTest {
    @Autowired private Day41TeacherMapper xml;
    @Autowired private Day42TeacherMapper plus;

    @Test void xmlMapsDifferentColumnName() {
        TeacherRow row = xml.findById(1L);
        assertEquals("张老师", row.getName());
        assertEquals("Java", row.getSubject());
    }
    @Test void xmlHandlesMissingAndMultipleRows() {
        assertNull(xml.findById(-1L));
        List<TeacherRow> rows = xml.findBySubject("Java");
        assertEquals(2, rows.size());
        assertTrue(rows.get(0).getId() < rows.get(1).getId());
        assertTrue(xml.findBySubject("不存在").isEmpty());
    }
    @Test void parameterBindingDoesNotTurnTextIntoSql() {
        assertTrue(xml.findBySubject("Java' OR '1'='1").isEmpty());
    }
    @Test void plusCrudWritesRealRows() {
        TeacherRow row = new TeacherRow(); row.setName("测试老师"); row.setSubject("测试科目");
        assertEquals(1, plus.insert(row)); assertNotNull(row.getId());
        assertEquals("测试老师", xml.findById(row.getId()).getName());
        row.setName("已更新"); assertEquals(1, plus.updateById(row));
        assertEquals("已更新", plus.selectById(row.getId()).getName());
        assertEquals(1, plus.deleteById(row.getId())); assertNull(xml.findById(row.getId()));
        assertEquals(0, plus.deleteById(row.getId()));
    }
    @Test void customAggregateMapsCount() {
        List<SubjectCount> counts = plus.countBySubject();
        assertEquals(2, counts.size());
        SubjectCount java = counts.stream().filter(c -> "Java".equals(c.getSubject())).findFirst().get();
        assertEquals(Long.valueOf(2), java.getTeacherCount());
    }
}
