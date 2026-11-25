USE java_learning_day25;

SELECT t.teacher_no, t.name, l.lesson_no, l.title
FROM teacher t JOIN lesson l ON l.teacher_id = t.id
ORDER BY t.id, l.id;

SELECT t.teacher_no, t.name, l.lesson_no, l.title
FROM teacher t LEFT JOIN lesson l ON l.teacher_id = t.id
ORDER BY t.id, l.id;

SELECT t.teacher_no, t.name, COUNT(l.id) AS lesson_count,
       COALESCE(AVG(l.duration_minutes), 0) AS average_minutes
FROM teacher t LEFT JOIN lesson l ON l.teacher_id = t.id
GROUP BY t.id, t.teacher_no, t.name ORDER BY t.id;

SELECT t.teacher_no, COUNT(l.id) AS lesson_count
FROM teacher t LEFT JOIN lesson l ON l.teacher_id = t.id
GROUP BY t.id, t.teacher_no HAVING COUNT(l.id) >= 2
ORDER BY t.teacher_no;

SELECT t.teacher_no, t.name, COUNT(DISTINCT l.id) AS lesson_count,
       COUNT(r.id) AS reviewed_lesson_count,
       COALESCE(AVG(r.score), 0) AS average_score
FROM teacher t
LEFT JOIN lesson l ON l.teacher_id = t.id
LEFT JOIN lesson_review r ON r.lesson_id = l.id
GROUP BY t.id, t.teacher_no, t.name ORDER BY t.id;
