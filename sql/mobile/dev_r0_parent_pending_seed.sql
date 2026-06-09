-- ============================================================
-- R0-1 verification only: one pending Parent (status=t)
-- NOT for production. Remove after R2 register API is live.
-- ============================================================

SET NAMES utf8mb4;

INSERT INTO `sys_user` (
  `user_id`, `user_name`, `login_name`, `login_pwd`, `status`,
  `user_role_id`, `user_phone`, `user_nick_name`, `root_org_id`, `create_time`
)
SELECT
  'dev-parent-pending-01',
  'Test Parent',
  '13800138001',
  u.`login_pwd`,
  't',
  'Parent',
  '13800138001',
  'Test Parent',
  u.`root_org_id`,
  NOW()
FROM `sys_user` u
WHERE u.`login_name` = 'zhangxm'
LIMIT 1
ON DUPLICATE KEY UPDATE
  `status` = 't',
  `user_role_id` = 'Parent',
  `user_phone` = '13800138001',
  `root_org_id` = VALUES(`root_org_id`),
  `update_time` = NOW();
