-- ====================================================================
-- TestLinkHub 数据库表结构设计
-- ====================================================================
-- 项目名称: TestLinkHub
-- 创建日期: 2025-06-15
-- 最后更新: 2025-06-17
-- 版本: v1.0.1
-- 描述: 包含用户管理、公司管理、新闻发布、课程管理等功能的完整数据库结构
-- 
-- 表结构包括:
-- - user: 用户信息表
-- - company: 公司信息表
-- - attachment: 附件信息表
-- - news: 新闻动态表
-- - news_related: 新闻附件关联表
-- - lesson: 课程信息表
-- - lesson_resources: 课程资源表
-- ====================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS test_link_hub DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE test_link_hub;

-- ====================================================================
-- 基础模块表 (用户, 公司, 附件)
-- ====================================================================


-- 用户表
CREATE TABLE `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '用户唯一标识符',
  `name` VARCHAR(100) NOT NULL COMMENT '用户姓名',
  `email` VARCHAR(255) NOT NULL COMMENT '邮箱地址',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `phone_number` VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
  `address` TEXT DEFAULT NULL COMMENT '地址信息',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `gender` ENUM('male', 'female', 'other') DEFAULT NULL COMMENT '性别',
  `company_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '公司ID',
  `role` ENUM('admin', 'user', 'manager', 'guest') DEFAULT 'user' COMMENT '用户角色',
  `status` ENUM('active', 'inactive', 'pending', 'suspended') DEFAULT 'pending' COMMENT '用户状态',
  `description` TEXT DEFAULT NULL COMMENT '用户描述/简介',
  `post_count` INT UNSIGNED DEFAULT 0 COMMENT '发帖数量',
  `lesson_count` INT UNSIGNED DEFAULT 0 COMMENT '课程数量',
  `meeting_count` INT UNSIGNED DEFAULT 0 COMMENT '会议数量',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uuid` (`uuid`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 公司表
CREATE TABLE `company` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公司主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '公司唯一标识符',
  `name` VARCHAR(200) NOT NULL COMMENT '公司名称',
  `email` VARCHAR(255) DEFAULT NULL COMMENT '公司邮箱',
  `password` VARCHAR(255) DEFAULT NULL COMMENT '公司密码（如果需要登录）',
  `phone_number` VARCHAR(20) DEFAULT NULL COMMENT '公司电话',
  `address` TEXT DEFAULT NULL COMMENT '公司地址',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '公司头像/LOGO URL',
  `company_code` VARCHAR(50) DEFAULT NULL COMMENT '公司代码/编号',
  `status` ENUM('active', 'inactive', 'pending', 'suspended') DEFAULT 'pending' COMMENT '公司状态',
  `description` TEXT DEFAULT NULL COMMENT '公司描述/简介',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_uuid` (`uuid`),
  UNIQUE KEY `uk_company_code` (`company_code`),
  KEY `idx_company_name` (`name`),
  KEY `idx_company_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司信息表';



-- 附件表
CREATE TABLE `attachment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '附件主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '附件唯一标识符',
  `name` VARCHAR(255) NOT NULL COMMENT '附件名称',
  `attachment_url` VARCHAR(500) NOT NULL COMMENT '附件URL地址',
  `company_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '所属公司ID',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attachment_uuid` (`uuid`),
  KEY `idx_attachment_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件信息表';


-- ====================================================================
-- 新闻模块表 (已采用版本控制设计)
-- ====================================================================

-- 新闻主表 (Master)
CREATE TABLE `news` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '新闻主键ID',
  `uuid` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '新闻唯一标识符',
  `company_id` bigint unsigned NOT NULL COMMENT '发布公司ID',
  `visible` tinyint(1) DEFAULT '1' COMMENT '是否可见（0:仅企业内部可见, 1:全部用户可见）',
  `status` enum('draft','published','archived') COLLATE utf8mb4_unicode_ci DEFAULT 'draft' COMMENT '新闻整体状态',
  `current_content_id` bigint unsigned DEFAULT NULL COMMENT '当前生效的内容版本ID',
  `pending_content_id` bigint unsigned DEFAULT NULL COMMENT '审核中的内容版本ID',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_uuid` (`uuid`),
  KEY `idx_news_company_id` (`company_id`),
  KEY `idx_news_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻主表 (Master)';

-- 新闻内容版本表 (Version)
CREATE TABLE `news_content` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '内容版本ID',
  `uuid` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容版本唯一标识符',
  `news_id` bigint unsigned NOT NULL COMMENT '关联新闻ID',
  `title` VARCHAR(255) NOT NULL COMMENT '新闻标题',
  `summary` TEXT DEFAULT NULL COMMENT '新闻摘要',
  `cover_image_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
  `resource_url` VARCHAR(255) COMMENT '新闻正文内容 (HTML或Markdown)',
  `version` int unsigned NOT NULL DEFAULT '1' COMMENT '版本号',
  `status` enum('draft','pending_review','active','rejected', 'archived') COLLATE utf8mb4_unicode_ci DEFAULT 'draft' COMMENT '版本状态',
  `publisher_id` BIGINT UNSIGNED NOT NULL COMMENT '此版本内容的创建/修改者ID',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '版本创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_content_uuid` (`uuid`),
  KEY `idx_news_content_news_id` (`news_id`),
  KEY `idx_news_content_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻内容版本表';

-- 新闻附件关联表
CREATE TABLE `news_attachment_related` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
  `news_content_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的新闻内容版本ID',
  `attachment_id` BIGINT UNSIGNED NOT NULL COMMENT '附件ID',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_content_attachment` (`news_content_id`, `attachment_id`),
  KEY `idx_related_news_content_id` (`news_content_id`),
  KEY `idx_related_attachment_id` (`attachment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻内容与附件关联表';

-- 新闻审核历史表
CREATE TABLE `news_audit_history` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审核记录主键ID',
  `news_content_id` BIGINT UNSIGNED NOT NULL COMMENT '被审核的新闻内容版本ID',
  `auditor_id` BIGINT UNSIGNED NOT NULL COMMENT '审核员ID',
  `audit_status` ENUM('approved', 'rejected') NOT NULL COMMENT '审核结果',
  `comments` TEXT COMMENT '审核意见或备注',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '审核操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_audit_news_content_id` (`news_content_id`),
  KEY `idx_audit_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻审核历史表';


-- ====================================================================
-- 课程模块表 (已采用版本控制设计)
-- ====================================================================

-- 课程主表 (Master)
CREATE TABLE `lesson` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '课程主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '课程唯一标识符',
  `publisher_id` BIGINT UNSIGNED NOT NULL COMMENT '课程的最终发布方/所有者ID',
  `status` ENUM('pending_review', 'active', 'rejected', 'archived') NOT NULL DEFAULT 'pending_review' COMMENT '课程的整体状态',
  `current_version_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '当前生效的版本ID',
  `pending_version_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '待审核的版本ID',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lesson_uuid` (`uuid`),
  KEY `idx_lesson_publisher_id` (`publisher_id`),
  KEY `idx_lesson_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程主表 (Master)';

-- 课程版本表 (Version)
CREATE TABLE `lesson_version` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '版本主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '版本唯一标识符',
  `lesson_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的课程主表ID',
  `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '版本号',
  `name` VARCHAR(255) NOT NULL COMMENT '课程名称',
  `description` TEXT DEFAULT NULL COMMENT '课程描述',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '课程封面图片URL',
  `author_name` VARCHAR(100) DEFAULT NULL COMMENT '作者名称',
  `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '课程排序',
  `status` ENUM('pending_review', 'active', 'rejected', 'archived') NOT NULL DEFAULT 'pending_review' COMMENT '版本状态',
  `creator_id` BIGINT UNSIGNED NOT NULL COMMENT '此版本的创建/修改者ID',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '版本创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lesson_version_uuid` (`uuid`),
  KEY `idx_lv_lesson_id` (`lesson_id`),
  KEY `idx_lv_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程内容版本表';

-- 课程资源表
CREATE TABLE `lesson_resources` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '课程资源主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '资源唯一标识符',
  `lesson_version_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的课程版本ID',
  `resources_url` VARCHAR(500) NOT NULL COMMENT '资源URL地址',
  `resources_type` ENUM('video', 'audio', 'document', 'image', 'link', 'other') NOT NULL COMMENT '资源类型',
  `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '资源在课程内的排序',
  `status` ENUM('active', 'inactive') DEFAULT 'active' COMMENT '资源状态',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lesson_resources_uuid` (`uuid`),
  KEY `idx_lr_lesson_version_id` (`lesson_version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程资源表';


-- 课程审核历史表
CREATE TABLE `lesson_audit_history` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审核记录主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '审核记录唯一标识符',
  `lesson_version_id` BIGINT UNSIGNED NOT NULL COMMENT '被审核的课程版本ID',
  `auditor_id` BIGINT UNSIGNED NOT NULL COMMENT '审核员ID',
  `audit_status` ENUM('approved', 'rejected') NOT NULL COMMENT '审核结果',
  `comments` TEXT COMMENT '审核意见或备注',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '审核操作时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `lesson_audit_history_pk` (`uuid`),
  KEY `idx_lah_lesson_version_id` (`lesson_version_id`),
  KEY `idx_lah_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程审核历史表';


-- ====================================================================
-- 会议模块表 (已采用版本控制设计)
-- ====================================================================

-- 会议主表 (Master)
CREATE TABLE `meeting` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会议主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '会议唯一标识符',
  `creator_id` BIGINT UNSIGNED NOT NULL COMMENT '会议创建者ID',
  `status` ENUM('draft', 'published', 'cancelled', 'completed') NOT NULL DEFAULT 'draft' COMMENT '会议整体状态',
  `current_version_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '当前生效的版本ID',
  `pending_version_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '待审核的版本ID',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_uuid` (`uuid`),
  KEY `idx_meeting_creator_id` (`creator_id`),
  KEY `idx_meeting_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议主表 (Master)';

-- 会议版本表 (Version)
CREATE TABLE `meeting_version` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '版本主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '版本唯一标识符',
  `meeting_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的会议主表ID',
  `version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '版本号',
  `name` VARCHAR(255) NOT NULL COMMENT '会议名称',
  `description` TEXT DEFAULT NULL COMMENT '会议内容/描述',
  `cover_image_url` VARCHAR(500) DEFAULT NULL COMMENT '会议封面图片URL',
  `start_time` TIMESTAMP NOT NULL COMMENT '会议开始时间',
  `end_time` TIMESTAMP NOT NULL COMMENT '会议结束时间',
  `status` ENUM('draft', 'pending_review', 'active', 'rejected', 'archived') NOT NULL DEFAULT 'draft' COMMENT '版本状态',
  `editor_id` BIGINT UNSIGNED NOT NULL COMMENT '此版本的创建/修改者ID',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '版本创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_version_uuid` (`uuid`),
  KEY `idx_mv_meeting_id` (`meeting_id`),
  KEY `idx_mv_status` (`status`),
  KEY `idx_mv_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议内容版本表';


-- 会议参会人员表
CREATE TABLE `meeting_participant` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '参会人员唯一标识符',
  `meeting_id` BIGINT UNSIGNED NOT NULL COMMENT '会议ID (关联 meeting.id)',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID (关联 user.id)',
  `join_reason` TEXT DEFAULT NULL COMMENT '申请参会原因',
  `status` ENUM('pending', 'approved', 'rejected', 'cancelled') NOT NULL DEFAULT 'pending' COMMENT '参会申请状态',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_participant_uuid` (`uuid`),
  UNIQUE KEY `uk_meeting_user` (`meeting_id`, `user_id`),
  KEY `idx_participant_meeting_id` (`meeting_id`),
  KEY `idx_participant_user_id` (`user_id`),
  KEY `idx_participant_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议参会人员表';

-- 会议审核历史表
CREATE TABLE `meeting_audit_history` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审核记录主键ID',
  `meeting_version_id` BIGINT UNSIGNED NOT NULL COMMENT '被审核的会议版本ID',
  `auditor_id` BIGINT UNSIGNED NOT NULL COMMENT '审核员ID',
  `audit_status` ENUM('approved', 'rejected') NOT NULL COMMENT '审核结果',
  `comments` TEXT COMMENT '审核意见或备注',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '审核操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_audit_meeting_version_id` (`meeting_version_id`),
  KEY `idx_audit_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议审核历史表';
-- ====================================================================
-- 通知模块表 (v3 - 简化类型)
-- ====================================================================

-- 通知主表 (存储通知的模板和源信息)
CREATE TABLE `notification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '通知唯一标识符',
  `sender_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '发送者ID (如果是用户操作触发), NULL表示系统',
  `title` VARCHAR(255) NOT NULL COMMENT '通知标题 (例如: "您的课程已被驳回")',
  `content` TEXT NOT NULL COMMENT '通知内容/描述 (包含具体原因或详情)',
  
  -- 核心修改点：使用您指定的四种分类
  `type` ENUM(
      'LESSON',       -- 课程相关
      'NEWS',         -- 新闻动态相关
      'MEETING',      -- 会议相关
      'APPLICATION'   -- 申请相关
  ) NOT NULL COMMENT '通知的业务模块分类',

  `related_object_type` ENUM('lesson', 'meeting', 'news', 'user', 'company') DEFAULT NULL COMMENT '关联对象类型',
  `related_object_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联对象ID (例如 lesson_id, meeting_id)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_uuid` (`uuid`),
  KEY `idx_notification_type` (`type`),
  KEY `idx_notification_related_object` (`related_object_type`, `related_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知主表';


-- 通知接收人关联表 (此表结构无需改动，依然适用)
CREATE TABLE `notification_recipient` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notification_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的通知ID',
  `recipient_id` BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
  `is_read` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已读 (0-未读, 1-已读)',
  `read_at` TIMESTAMP NULL DEFAULT NULL COMMENT '读取时间',
  `is_deleted` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否被用户删除 (0-未删除, 1-已删除)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_recipient` (`notification_id`, `recipient_id`),
  KEY `idx_nr_recipient_id_is_read` (`recipient_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知接收人及状态表';



-- ====================================================================
-- 附加索引及逻辑外键说明
-- ====================================================================

-- 逻辑关联关系说明 (外键约束由应用层保证):
-- user.company_id -> company.id
-- attachment.company_id -> company.id
-- news.company_id -> company.id
-- news_content.news_id -> news.id
-- news_content.publisher_id -> user.id
-- news_attachment_related.news_content_id -> news_content.id
-- news_attachment_related.attachment_id -> attachment.id
-- news_audit_history.news_content_id -> news_content.id
-- news_audit_history.auditor_id -> user.id
-- lesson.publisher_id -> user.id
-- lesson_version.lesson_id -> lesson.id
-- lesson_version.creator_id -> user.id
-- lesson_resources.lesson_version_id -> lesson_version.id
-- lesson_audit_history.lesson_version_id -> lesson_version.id
-- lesson_audit_history.auditor_id -> user.id
-- meeting.creator_id -> user.id
-- meeting_version.meeting_id -> meeting.id
-- meeting_version.editor_id -> user.id
-- meeting_participant.meeting_id -> meeting.id
-- meeting_participant.user_id -> user.id
-- meeting_audit_history.meeting_version_id -> meeting_version.id
-- meeting_audit_history.auditor_id -> user.id

-- 创建附加索引以优化查询性能
CREATE INDEX `idx_user_name` ON `user` (`name`);
CREATE INDEX `idx_user_phone` ON `user` (`phone_number`);
CREATE INDEX `idx_user_counts` ON `user` (`post_count`, `lesson_count`, `meeting_count`);
CREATE INDEX `idx_company_phone` ON `company` (`phone_number`);
