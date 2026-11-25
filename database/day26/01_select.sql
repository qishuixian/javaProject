-- 前提：Day25 建表与示例数据已成功执行。只读查询。
USE java_learning_day25;
SELECT DATABASE() AS current_database;

SELECT id, teacher_no, name FROM teacher ORDER BY id;
SELECT id, teacher_no, name FROM teacher WHERE teacher_no = 'T001';
SELECT lesson_no, title, duration_minutes FROM lesson
WHERE duration_minutes >= 45 AND duration_minutes <= 60 ORDER BY id;
SELECT lesson_no, title FROM lesson
WHERE lesson_no IN ('L001', 'L002') ORDER BY id;
SELECT teacher_no, name FROM teacher WHERE name LIKE '张%' ORDER BY id;

-- 左闭右开：包含 25 日零点，不包含 26 日零点。
SELECT lesson_no, starts_at FROM lesson
WHERE starts_at >= '2026-11-25 00:00:00'
  AND starts_at < '2026-11-26 00:00:00'
ORDER BY starts_at, id;

-- 排序增加唯一 ID 兜底，避免同名记录顺序不明确。
SELECT teacher_no, name FROM teacher ORDER BY name, id LIMIT 1 OFFSET 0;
SELECT teacher_no, name FROM teacher ORDER BY name, id LIMIT 1 OFFSET 1;

SELECT id, score, remark FROM lesson_review WHERE remark IS NULL ORDER BY id;
SELECT id, score, remark FROM lesson_review WHERE remark = '' ORDER BY id;
SELECT id, score, remark FROM lesson_review
WHERE remark IS NOT NULL AND remark <> '' ORDER BY id;

-- 三值逻辑：= NULL 的结果不是 true 或 false，而是 UNKNOWN（这里显示 NULL）。
SELECT NULL = NULL AS wrong_null_comparison,
       NULL IS NULL AS correct_null_check,
       '' IS NULL AS empty_is_null,
       CHAR_LENGTH('') AS empty_length;
