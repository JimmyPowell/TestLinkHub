ALTER TABLE `lesson_resources`
ADD COLUMN `name` VARCHAR(255) NOT NULL COMMENT '资源名称' AFTER `lesson_version_id`;
