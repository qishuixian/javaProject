import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeacherDirectoryTest {
    @Test
    void addAndFindTeacher() {
        // Arrange：准备独立数据。
        TeacherDirectory directory = new TeacherDirectory(2);
        // Act：执行被测操作。
        directory.add(10001L, " 张老师 ");
        // Assert：自动检查结果，而不只是打印。
        Teacher found = directory.findById(10001L);
        assertNotNull(found);
        assertEquals("张老师", found.getName());
        assertEquals(1, directory.size());
    }

    @Test
    void missingTeacherReturnsNull() {
        TeacherDirectory directory = new TeacherDirectory(2);
        assertNull(directory.findById(99999L));
    }

    @Test
    void duplicateIdDoesNotReplaceOriginal() {
        TeacherDirectory directory = new TeacherDirectory(2);
        directory.add(10001L, "张老师");
        assertThrows(IllegalArgumentException.class,
                () -> directory.add(10001L, "李老师"));
        assertEquals(1, directory.size());
        assertEquals("张老师", directory.findById(10001L).getName());
    }

    @Test
    void blankNameDoesNotConsumeCapacity() {
        TeacherDirectory directory = new TeacherDirectory(1);
        assertThrows(IllegalArgumentException.class,
                () -> directory.add(10001L, "   "));
        assertEquals(0, directory.size());
        directory.add(10002L, "李老师");
        assertEquals(1, directory.size());
    }

    @Test
    void fullDirectoryPreservesExistingData() {
        TeacherDirectory directory = new TeacherDirectory(1);
        directory.add(10001L, "张老师");
        assertThrows(IllegalArgumentException.class,
                () -> directory.add(10002L, "李老师"));
        assertEquals(1, directory.size());
        assertNull(directory.findById(10002L));
        assertEquals("张老师", directory.findById(10001L).getName());
    }

    @Test
    void invalidRenameKeepsOldName() {
        TeacherDirectory directory = new TeacherDirectory(1);
        directory.add(10001L, "张老师");
        assertThrows(IllegalArgumentException.class,
                () -> directory.rename(10001L, null));
        assertEquals("张老师", directory.findById(10001L).getName());
        directory.rename(10001L, "李老师");
        assertEquals("李老师", directory.findById(10001L).getName());
    }
}
