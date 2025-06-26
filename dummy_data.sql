-- ====================================================================
-- TestLinkHub 示例用户数据
-- ====================================================================
-- 注意:
-- 1. 密码是明文占位符，实际使用时应替换为BCrypt加密后的哈希值。
-- 2. company_id 假设为已存在于 `company` 表中的ID。
-- ====================================================================

-- 1. 管理员用户 (Admin)
INSERT INTO `user` 
(`uuid`, `name`, `email`, `password`, `phone_number`, `address`, `avatar_url`, `gender`, `company_id`, `role`, `status`, `description`, `is_deleted`, `created_at`, `updated_at`) 
VALUES
('a1b2c3d4-e5f6-7890-1234-567890abcdef', 'Admin User', 'admin@testlinkhub.com', '$2a$10$your_bcrypt_hashed_password_here', '12345678900', '123 Admin Street, Admin City', 'https://example.com/avatar/admin.png', 'other', NULL, 'admin', 'active', 'Super administrator with all privileges.', 0, NOW(), NOW());

-- 2. 公司管理员用户 (Manager)
INSERT INTO `user` 
(`uuid`, `name`, `email`, `password`, `phone_number`, `address`, `avatar_url`, `gender`, `company_id`, `role`, `status`, `description`, `is_deleted`, `created_at`, `updated_at`) 
VALUES
('b2c3d4e5-f6a7-8901-2345-67890abcdef1', 'Manager Alice', 'manager.alice@example.com', '$2a$10$your_bcrypt_hashed_password_here', '12345678901', '456 Manager Avenue, Business City', 'https://example.com/avatar/manager_alice.png', 'female', 1, 'manager', 'active', 'Manager for Company ID 1.', 0, NOW(), NOW());

-- 3. 活跃的普通用户 (Active User)
INSERT INTO `user` 
(`uuid`, `name`, `email`, `password`, `phone_number`, `address`, `avatar_url`, `gender`, `company_id`, `role`, `status`, `description`, `is_deleted`, `created_at`, `updated_at`) 
VALUES
('c3d4e5f6-a7b8-9012-3456-7890abcdef12', 'Bob Johnson', 'bob.j@example.com', '$2a$10$your_bcrypt_hashed_password_here', '12345678902', '789 User Lane, People Town', 'https://example.com/avatar/bob.png', 'male', 1, 'user', 'active', 'An active standard user.', 0, NOW(), NOW());

-- 4. 不活跃的普通用户 (Inactive User)
INSERT INTO `user` 
(`uuid`, `name`, `email`, `password`, `phone_number`, `address`, `avatar_url`, `gender`, `company_id`, `role`, `status`, `description`, `is_deleted`, `created_at`, `updated_at`) 
VALUES
('d4e5f6a7-b8c9-0123-4567-890abcdef123', 'Charlie Brown', 'charlie.b@example.com', '$2a$10$your_bcrypt_hashed_password_here', '12345678903', '101 Inactive Road, Sleepy Hollow', 'https://example.com/avatar/charlie.png', 'male', 2, 'user', 'inactive', 'An inactive user who has not logged in for a while.', 0, NOW(), NOW());

-- 5. 被暂停的普通用户 (Suspended User)
INSERT INTO `user` 
(`uuid`, `name`, `email`, `password`, `phone_number`, `address`, `avatar_url`, `gender`, `company_id`, `role`, `status`, `description`, `is_deleted`, `created_at`, `updated_at`) 
VALUES
('e5f6a7b8-c9d0-1234-5678-90abcdef1234', 'Diana Prince', 'diana.p@example.com', '$2a$10$your_bcrypt_hashed_password_here', '12345678904', '222 Suspended Plaza, Justice City', 'https://example.com/avatar/diana.png', 'female', NULL, 'user', 'suspended', 'A user who has been suspended due to policy violations.', 0, NOW(), NOW());
