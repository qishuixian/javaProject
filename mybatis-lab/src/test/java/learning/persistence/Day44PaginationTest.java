package learning.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class Day44PaginationTest {
    @Autowired private TeacherPageService service;
    @Autowired private Day44TeacherMapper mapper;

    @Test void distinguishesThreeCounts() {
        TeacherPage page = service.query(" Java ", 1, 1);
        assertEquals(1, page.getCurrentCount()); assertEquals(2, page.getTotal());
        assertEquals(3, mapper.countAll()); assertEquals(2, page.getTotalPages());
    }
    @Test void tiedSubjectsHaveStableUniqueOrder() {
        TeacherPage first = service.query("Java", 1, 1);
        TeacherPage second = service.query("Java", 2, 1);
        assertEquals(Long.valueOf(1), first.getRecords().get(0).getId());
        assertEquals(Long.valueOf(2), second.getRecords().get(0).getId());
        assertEquals(first.getRecords().get(0).getId(), service.query("Java",1,1).getRecords().get(0).getId());
    }
    @Test void lastPageCanBePartial() {
        TeacherPage page = service.query(null, 2, 2);
        assertEquals(1, page.getCurrentCount()); assertEquals(3, page.getTotal());
        assertEquals(2, page.getTotalPages());
    }
    @Test void beyondLastPageKeepsTotal() {
        TeacherPage page = service.query("Java", 3, 1);
        assertTrue(page.getRecords().isEmpty()); assertEquals(2, page.getTotal());
    }
    @Test void noMatchesHasZeroPages() {
        TeacherPage page = service.query("不存在", 1, 20);
        assertEquals(0, page.getTotal()); assertEquals(0, page.getTotalPages());
        assertTrue(page.getRecords().isEmpty());
    }
    @Test void blankAndInjectionLikeValuesAreHandled() {
        assertEquals(3, service.query("  ", 1, 100).getTotal());
        assertEquals(0, service.query("Java' OR '1'='1", 1, 20).getTotal());
    }
    @Test void rejectsInvalidPageArguments() {
        assertThrows(IllegalArgumentException.class, () -> service.query(null,0,20));
        assertThrows(IllegalArgumentException.class, () -> service.query(null,-1,20));
        assertThrows(IllegalArgumentException.class, () -> service.query(null,1,0));
        assertThrows(IllegalArgumentException.class, () -> service.query(null,1,101));
    }
    @Test void largeOffsetDoesNotOverflowInt() {
        TeacherPage page = service.query(null, Integer.MAX_VALUE, 100);
        assertTrue(page.getRecords().isEmpty()); assertEquals(3, page.getTotal());
    }
}
