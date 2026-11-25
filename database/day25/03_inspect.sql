-- 只读检查，不触发预计失败的写入操作。
USE java_learning_day25;
SELECT VERSION() AS server_version, DATABASE() AS current_database;
SHOW CREATE TABLE teacher;
SHOW CREATE TABLE lesson;
SHOW CREATE TABLE lesson_review;

SELECT 'teacher' AS table_name, COUNT(*) AS row_count FROM teacher
UNION ALL
SELECT 'lesson', COUNT(*) FROM lesson
UNION ALL
SELECT 'lesson_review', COUNT(*) FROM lesson_review;

-- L002 没有审核，LEFT JOIN 保留它，审核字段显示 NULL。
SELECT l.lesson_no, t.teacher_no, t.name, l.title, r.score, r.remark
FROM lesson l
JOIN teacher t ON t.id = l.teacher_id
LEFT JOIN lesson_review r ON r.lesson_id = l.id
ORDER BY l.lesson_no;
