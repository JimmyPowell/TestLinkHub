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
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uuid` (`uuid`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`),
  KEY `idx_role` (`role`),
  KEY `idx_created_at` (`created_at`)
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
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_uuid` (`uuid`),
  UNIQUE KEY `uk_company_code` (`company_code`),
  KEY `idx_company_email` (`email`),
  KEY `idx_company_status` (`status`),
  KEY `idx_company_name` (`name`),
  KEY `idx_company_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司信息表';

-- 附件表
CREATE TABLE `attachment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '附件主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '附件唯一标识符',
  `name` VARCHAR(255) NOT NULL COMMENT '附件名称',
  `attachment_url` VARCHAR(500) NOT NULL COMMENT '附件URL地址',
  `company_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '所属公司ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attachment_uuid` (`uuid`),
  KEY `idx_attachment_company_id` (`company_id`),
  KEY `idx_attachment_name` (`name`),
  KEY `idx_attachment_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件信息表';

-- 新闻动态表
CREATE TABLE `news` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '新闻主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '新闻唯一标识符',
  `company_id` BIGINT UNSIGNED NOT NULL COMMENT '发布公司ID',
  `content_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '内容ID（如果有独立的内容表）',
  `visible` TINYINT(1) DEFAULT 1 COMMENT '是否可见（0:不可见, 1:可见）',
  `status` ENUM('draft', 'published', 'archived', 'deleted') DEFAULT 'draft' COMMENT '新闻状态',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_uuid` (`uuid`),
  KEY `idx_news_company_id` (`company_id`),
  KEY `idx_news_content_id` (`content_id`),
  KEY `idx_news_visible` (`visible`),
  KEY `idx_news_status` (`status`),
  KEY `idx_news_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻动态表';

-- 新闻关联表（中间表）
CREATE TABLE `news_related` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
  `news_id` BIGINT UNSIGNED NOT NULL COMMENT '新闻ID',
  `attachment_id` BIGINT UNSIGNED NOT NULL COMMENT '附件ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_attachment` (`news_id`, `attachment_id`),
  KEY `idx_news_related_news_id` (`news_id`),
  KEY `idx_news_related_attachment_id` (`attachment_id`),
  KEY `idx_news_related_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻附件关联表';

-- 课程表
CREATE TABLE `lesson` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '课程主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '课程唯一标识符',
  `name` VARCHAR(255) NOT NULL COMMENT '课程名称',
  `publisher_id` BIGINT UNSIGNED NOT NULL COMMENT '发布者ID（用户或公司）',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '课程封面图片URL',
  `description` TEXT DEFAULT NULL COMMENT '课程描述',
  `sort_order` INT UNSIGNED DEFAULT 0 COMMENT '课程排序，数值越小越靠前',
  `author_name` VARCHAR(100) DEFAULT NULL COMMENT '作者名称',
  `visible` TINYINT(1) DEFAULT 1 COMMENT '是否可见（0:不可见, 1:可见）',
  `status` ENUM('pending_review', 'published', 'rejected', 'archived') NOT NULL DEFAULT 'pending_review' COMMENT '课程状态 (pending_review:待审核, published:已发布, rejected:审核不通过, archived:已归档)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lesson_uuid` (`uuid`),
  KEY `idx_lesson_publisher_id` (`publisher_id`),
  KEY `idx_lesson_name` (`name`),
  KEY `idx_lesson_visible` (`visible`),
  KEY `idx_lesson_status` (`status`),
  KEY `idx_lesson_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程信息表';

-- 课程资源表
CREATE TABLE `lesson_resources` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '课程资源主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '资源唯一标识符',
  `resources_url` VARCHAR(500) NOT NULL COMMENT '资源URL地址',
  `resources_type` ENUM('video', 'audio', 'document', 'image', 'link', 'other') NOT NULL COMMENT '资源类型',
  `publisher_id` BIGINT UNSIGNED NOT NULL COMMENT '发布者ID',
  `queue_id` INT UNSIGNED DEFAULT NULL COMMENT '队列ID/排序ID',
  `status` ENUM('active', 'inactive', 'pending', 'processing', 'failed') DEFAULT 'pending' COMMENT '资源状态',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lesson_resources_uuid` (`uuid`),
  KEY `idx_lesson_resources_publisher_id` (`publisher_id`),
  KEY `idx_lesson_resources_type` (`resources_type`),
  KEY `idx_lesson_resources_queue_id` (`queue_id`),
  KEY `idx_lesson_resources_status` (`status`),
  KEY `idx_lesson_resources_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程资源表';

-- 课程审核历史表
CREATE TABLE `lesson_audit_history` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审核记录主键ID',
  `lesson_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的课程ID',
  `auditor_id` BIGINT UNSIGNED NOT NULL COMMENT '审核员ID (关联 user.id)',
  `audit_status` ENUM('approved', 'rejected') NOT NULL COMMENT '审核结果 (approved:通过, rejected:不通过)',
  `comments` TEXT COMMENT '审核意见或备注',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '审核操作时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_lesson_id` (`lesson_id`),
  KEY `idx_auditor_id` (`auditor_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程审核历史表';

-- ====================================================================
-- 会议模块表
-- ====================================================================

-- 会议表
CREATE TABLE `meeting` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会议主键ID',
  `uuid` VARCHAR(36) NOT NULL COMMENT '会议唯一标识符',
  `name` VARCHAR(255) NOT NULL COMMENT '会议名称',
  `description` TEXT DEFAULT NULL COMMENT '会议内容/描述',
  `cover_image_url` VARCHAR(500) DEFAULT NULL COMMENT '会议封面图片URL',
  `creator_id` BIGINT UNSIGNED NOT NULL COMMENT '会议创建者ID (关联 user.id)',
  `start_time` TIMESTAMP NOT NULL COMMENT '会议开始时间',
  `end_time` TIMESTAMP NOT NULL COMMENT '会议结束时间',
  `status` ENUM('pending_review', 'published', 'rejected', 'cancelled', 'completed') NOT NULL DEFAULT 'pending_review' COMMENT '会议状态 (pending_review:待审核, published:已发布, rejected:审核不通过, cancelled:已取消, completed:已完成)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_uuid` (`uuid`),
  KEY `idx_meeting_creator_id` (`creator_id`),
  KEY `idx_meeting_status` (`status`),
  KEY `idx_meeting_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议信息表';

-- 会议参会人员表
CREATE TABLE `meeting_participant` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `meeting_id` BIGINT UNSIGNED NOT NULL COMMENT '会议ID (关联 meeting.id)',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID (关联 user.id)',
  `join_reason` TEXT DEFAULT NULL COMMENT '申请参会原因',
  `status` ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending' COMMENT '参会申请状态 (pending:待处理, approved:已批准, rejected:已拒绝)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_meeting_user` (`meeting_id`, `user_id`),
  KEY `idx_participant_meeting_id` (`meeting_id`),
  KEY `idx_participant_user_id` (`user_id`),
  KEY `idx_participant_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议参会人员表';

-- 会议审核历史表
CREATE TABLE `meeting_audit_history` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '审核记录主键ID',
  `meeting_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的会议ID',
  `auditor_id` BIGINT UNSIGNED NOT NULL COMMENT '审核员ID (关联 user.id)',
  `audit_status` ENUM('approved', 'rejected') NOT NULL COMMENT '审核结果 (approved:通过, rejected:不通过)',
  `comments` TEXT COMMENT '审核意见或备注',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '审核操作时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_audit_meeting_id` (`meeting_id`),
  KEY `idx_audit_auditor_id` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议审核历史表';

-- 外键约束已移除，使用应用层逻辑外键
-- 逻辑关联关系说明：
-- user.company_id -> company.id
-- attachment.company_id -> company.id  
-- news.company_id -> company.id
-- news_related.news_id -> news.id
-- news_related.attachment_id -> attachment.id
-- lesson.publisher_id -> user.id
-- lesson_resources.publisher_id -> user.id

-- 创建索引以优化查询性能
CREATE INDEX `idx_user_name` ON `user` (`name`);
CREATE INDEX `idx_user_phone` ON `user` (`phone_number`);
CREATE INDEX `idx_user_counts` ON `user` (`post_count`, `lesson_count`, `meeting_count`);
CREATE INDEX `idx_company_phone` ON `company` (`phone_number`);
