-- 仅在本地独立学习库执行。需要 Day25 三张表，数据由本文件自己准备。
-- 必须在同一连接完整执行；发生错误立即停止并 ROLLBACK。
-- 不要将最后的 ROLLBACK 改成 COMMIT；不要在已有事务中运行。
USE java_learning_day25;
START TRANSACTION;

-- 唯一业务编号让正常重跑不依赖固定 ID，不操作 Day25 原有行。
SET @day26_teacher_no = CONCAT('D26T', LEFT(REPLACE(UUID(), '-', ''), 28));
SET @day26_lesson_no = CONCAT('D26L', LEFT(REPLACE(UUID(), '-', ''), 28));

INSERT INTO teacher (teacher_no, name) VALUES (@day26_teacher_no, 'Day26练习老师');
SET @day26_teacher_id = LAST_INSERT_ID();
SELECT id, teacher_no, name FROM teacher WHERE id = @day26_teacher_id;

INSERT INTO lesson (lesson_no, teacher_id, title, starts_at, duration_minutes)
VALUES (@day26_lesson_no, @day26_teacher_id, 'Day26 SQL 练习',
        '2026-11-25 19:30:00', 45);
SET @day26_lesson_id = LAST_INSERT_ID();
INSERT INTO lesson_review (lesson_id, score, remark, reviewed_at)
VALUES (@day26_lesson_id, 90.00, NULL, '2026-11-25 20:30:00');
SET @day26_review_id = LAST_INSERT_ID();

-- 更新前先预览目标。唯一主键条件只命中这次新建的教师。
SELECT id, name FROM teacher WHERE id = @day26_teacher_id;
UPDATE teacher SET name = 'Day26已改名老师' WHERE id = @day26_teacher_id;
SELECT ROW_COUNT() AS updated_teacher_rows;
SELECT id, name FROM teacher WHERE id = @day26_teacher_id;

-- NULL -> 空字符串：前者未提供，后者明确保存了长度为 0 的文本。
SELECT remark, remark IS NULL AS is_null, CHAR_LENGTH(remark) AS text_length
FROM lesson_review WHERE id = @day26_review_id;
UPDATE lesson_review SET remark = '' WHERE id = @day26_review_id;
SELECT remark, remark IS NULL AS is_null, CHAR_LENGTH(remark) AS text_length
FROM lesson_review WHERE id = @day26_review_id;

-- 记录一个保存点，在删除后恢复，以观察 DELETE 的作用。
SAVEPOINT before_delete;
SELECT id, lesson_id FROM lesson_review WHERE id = @day26_review_id;
DELETE FROM lesson_review WHERE id = @day26_review_id;
SELECT ROW_COUNT() AS deleted_review_rows;
SELECT COUNT(*) AS reviews_after_delete FROM lesson_review WHERE id = @day26_review_id;
ROLLBACK TO SAVEPOINT before_delete;
SELECT COUNT(*) AS reviews_after_restore FROM lesson_review WHERE id = @day26_review_id;

-- 回滚整个事务：新增、改名与备注修改均不保留。
ROLLBACK;
SELECT COUNT(*) AS teachers_after_rollback FROM teacher WHERE teacher_no = @day26_teacher_no;
SELECT COUNT(*) AS lessons_after_rollback FROM lesson WHERE lesson_no = @day26_lesson_no;
-- 最后两个计数应为 0；自增 ID 消耗不保证回退。
