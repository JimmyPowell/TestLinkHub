-- ====================================================================
-- TestLinkHub 用户表测试数据
-- ====================================================================
-- 注意:
-- 1. 密码是明文存储的 'password123'，在实际应用中应使用强哈希算法（如 bcrypt）进行加密。
-- 2. UUID 是示例，实际应用中应由程序生成。
-- 3. company_id = 1 是一个示例，请确保公司表中存在ID为1的记录。
-- ====================================================================

-- 1. 管理员用户 (Admin)
INSERT INTO `user` 
(`uuid`, `name`, `email`, `password`, `phone_number`, `address`, `gender`, `role`, `status`, `description`)
VALUES
('a1b2c3d4-0001-4001-8001-000000000001', 'Admin User', 'admin@testlinkhub.com', 'password123', '18800000001', '123 Admin St, Management City', 'male', 'admin', 'active', '系统最高管理员，拥有所有权限。');

-- 2. 公司经理 (Manager) - 关联到公司ID为1
INSERT INTO `user` 
(`uuid`, `name`, `email`, `password`, `phone_number`, `address`, `gender`, `company_id`, `role`, `status`, `description`)
VALUES
('a1b2c3d4-0002-4002-8002-000000000002', 'Manager Li', 'manager.li@examplecorp.com', 'password123', '18800000002', '456 Corporate Ave, Business City', 'female', 1, 'manager', 'active', 'Example Corp 的项目经理。');

-- 3. 普通活跃用户 (Active User) - 关联到公司ID为1
INSERT INTO `user` 
(`uuid`, `name`, `email`, `password`, `phone_number`, `address`, `gender`, `company_id`, `role`, `status`, `description`)
VALUES
('a1b2c3d4-0003-4003-8003-000000000003', 'Active Zhang', 'active.zhang@examplecorp.com', 'password123', '18800000003', '789 User Ln, Tech Park', 'male', 1, 'user', 'active', '一位活跃的普通用户。');

-- 4. 待审核的普通用户 (Pending User)
INSERT INTO `user` 
(`uuid`, `name`, `email`, `password`, `phone_number`, `role`, `status`, `description`)
VALUES
('a1b2c3d4-0004-4004-8004-000000000004', 'Pending Wang', 'pending.wang@newuser.com', 'password123', '18800000004', 'user', 'pending', '等待管理员审核的新注册用户。');

-- 5. 被暂停的用户 (Suspended User)
INSERT INTO `user` 
(`uuid`, `name`, `email`, `password`, `phone_number`, `role`, `status`, `description`)
VALUES
('a1b2c3d4-0005-4005-8005-000000000005', 'Suspended Zhao', 'suspended.zhao@olduser.com', 'password123', '18800000005', 'user', 'suspended', '因违反规定被暂停使用的用户。');
