-- Day25：仅用于本地独立学习库。兼容 MySQL 5.7+/8.x 常见语法。
-- 不删除、不覆盖已有表；若重复执行报“表已存在”，先检查已有结构。
CREATE DATABASE IF NOT EXISTS java_learning_day25
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE java_learning_day25;

CREATE TABLE teacher (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '教师主键',
    teacher_no VARCHAR(32) NOT NULL COMMENT '业务编号',
    name VARCHAR(64) NOT NULL COMMENT '教师姓名，允许同名',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_no (teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lesson (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '课节主键',
    lesson_no VARCHAR(32) NOT NULL COMMENT '课节业务编号',
    teacher_id BIGINT NOT NULL COMMENT '授课教师',
    title VARCHAR(128) NOT NULL,
    starts_at DATETIME NOT NULL COMMENT '本课约定保存 Asia/Shanghai 本地时间',
    duration_minutes INT NOT NULL COMMENT '应用必须校验大于 0',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_lesson_no (lesson_no),
    KEY idx_lesson_teacher_start (teacher_id, starts_at),
    CONSTRAINT fk_lesson_teacher FOREIGN KEY (teacher_id)
        REFERENCES teacher (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lesson_review (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核记录主键',
    lesson_id BIGINT NOT NULL COMMENT '每课节最多一份当前审核结果',
    score DECIMAL(5,2) NOT NULL COMMENT '应用必须校验 0 至 100',
    remark VARCHAR(500) NULL COMMENT '可选备注',
    reviewed_at DATETIME NOT NULL COMMENT '实际审核时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_review_lesson (lesson_id),
    CONSTRAINT fk_review_lesson FOREIGN KEY (lesson_id)
        REFERENCES lesson (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
