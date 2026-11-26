package learning.persistence;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static java.util.Arrays.asList;

@SpringBootTest
@Transactional
class Day43DynamicSqlTest {
    @Autowired private TeacherSearchService search;
    @Autowired private Day43TeacherMapper mapper;

    private List<Long> ids(List<TeacherRow> rows) {
        return rows.stream().map(TeacherRow::getId).collect(Collectors.toList());
    }
    @Test void noFiltersAndBlankFiltersReturnAll() {
        assertEquals(asList(1L, 2L, 3L), ids(search.search(null, null, null, null, null)));
        assertEquals(3, search.search("  ", " ", null, " ", " ").size());
    }
    @Test void eachFilterWorksIndependently() {
        assertEquals(asList(1L), ids(search.search("张老师", null, null, null, null)));
        assertEquals(asList(1L, 2L), ids(search.search(null, "Java", null, null, null)));
        assertEquals(asList(2L, 3L), ids(search.search(null, null, 2L, null, null)));
    }
    @Test void filtersCombineWithAndAndTrim() {
        assertEquals(asList(2L), ids(search.search(" 李老师 ", " Java ", 2L, "id", "desc")));
        assertTrue(search.search("张老师", "数学", null, null, null).isEmpty());
    }
    @Test void descendingAndTieBreakerAreStable() {
        assertEquals(asList(3L, 2L, 1L), ids(search.search(null, null, null, " ID ", " DESC ")));
        assertEquals(asList(1L, 2L), ids(search.search(null, "Java", null, "subject", "desc")));
    }
    @Test void nameSortMapsToRealColumn() {
        List<TeacherRow> rows = search.search(null, null, null, "name", "asc");
        assertEquals(3, rows.size());
        // H2 本实验默认字符串排序，验证 name 被映射为 teacher_name 而非固定常量。
        List<String> names = rows.stream().map(TeacherRow::getName).collect(Collectors.toList());
        List<String> sorted = names.stream().sorted().collect(Collectors.toList());
        assertEquals(sorted, names);
    }
    @Test void valuesAreBoundRatherThanExecuted() {
        assertTrue(search.search(null, "Java' OR '1'='1", null, null, null).isEmpty());
        assertTrue(search.search("张老师' OR 1=1 --", null, null, null, null).isEmpty());
    }
    @Test void invalidSortDirectionAndMinIdAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> search.search(null,null,null,"id; DROP TABLE teacher","asc"));
        assertThrows(IllegalArgumentException.class, () -> search.search(null,null,null,"teacher_name","asc"));
        assertThrows(IllegalArgumentException.class, () -> search.search(null,null,null,"id","desc; --"));
        assertThrows(IllegalArgumentException.class, () -> search.search(null,null,0L,null,null));
        assertEquals(3, search.search(null,null,null,null,null).size());
    }
    @Test void mapperBranchesNeverPasteUnknownSortText() {
        assertEquals(asList(1L, 2L, 3L), ids(mapper.search(null,null,null,"id; DROP TABLE teacher","desc; --")));
    }
}
