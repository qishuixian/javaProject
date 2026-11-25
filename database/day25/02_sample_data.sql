-- 在 01_schema.sql 成功后，对空练习表执行一次。
USE java_learning_day25;
START TRANSACTION;

INSERT INTO teacher (teacher_no, name) VALUES ('T001', '张老师');
SET @day25_teacher_id = LAST_INSERT_ID();
INSERT INTO teacher (teacher_no, name) VALUES ('T002', '张老师');

INSERT INTO lesson
    (lesson_no, teacher_id, title, starts_at, duration_minutes)
VALUES
    ('L001', @day25_teacher_id, 'Java 入门', '2026-11-25 19:30:00', 45);
SET @day25_first_lesson_id = LAST_INSERT_ID();

INSERT INTO lesson
    (lesson_no, teacher_id, title, starts_at, duration_minutes)
VALUES
    ('L002', @day25_teacher_id, '集合基础', '2026-11-26 19:30:00', 60);

INSERT INTO lesson_review (lesson_id, score, remark, reviewed_at)
VALUES (@day25_first_lesson_id, 88.50, NULL, '2026-11-25 20:30:00');

COMMIT;
-- 任一语句失败时停止执行并 ROLLBACK，不要继续 COMMIT。
