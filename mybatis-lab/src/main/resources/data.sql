INSERT INTO teacher (teacher_name, subject) VALUES ('张老师', 'Java');
INSERT INTO teacher (teacher_name, subject) VALUES ('李老师', 'Java');
INSERT INTO teacher (teacher_name, subject) VALUES ('王老师', '数学');
-- 保留缺失教师引用，专门演示补充信息时的缺失处理，不设置外键。
INSERT INTO lesson48 VALUES (1, 2), (2, 1), (3, 2), (4, 999), (5, 3);
