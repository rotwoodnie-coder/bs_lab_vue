-- ============================================================
-- Mobile admin menus (R0)
-- Role-menu binding: use admin UI Role Management, NOT this file.
-- ============================================================

SET NAMES utf8mb4;

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `menu_name`, `menu_code`, `menu_type`, `path`, `component`, `sort_order`, `status`, `comments`, `create_user_id`, `update_user_id`) VALUES
('mobile-mgmt-001', '0', '移动端管理', 'mobile-management', 'menu', '/admin/mobile', NULL, 70, 'y', 'Mobile R0 admin group', NULL, NULL),
('mobile-mgmt-002', 'mobile-mgmt-001', '家长注册审核', 'mobile-parent-audit', 'page', '/admin/mobile/parent-registrations', 'views/mobile-admin/ParentRegistrationAuditView.vue', 10, 'y', 'Parent sys_user status t audit', NULL, NULL),
('mobile-mgmt-003', 'mobile-mgmt-001', '家长绑定审核', 'mobile-bind-audit', 'page', '/admin/mobile/parent-binds', 'views/mobile-admin/ParentBindAuditView.vue', 20, 'y', 'mb_parent_child pending audit', NULL, NULL),
('mobile-mgmt-004', 'mobile-mgmt-001', '勋章规则管理', 'mobile-badge-mgmt', 'page', '/admin/mobile/badges', 'views/mobile-admin/BadgeManagementView.vue', 30, 'y', 'mb_badge_def CRUD', NULL, NULL),
('mobile-mgmt-005', 'mobile-mgmt-001', '评论管理', 'mobile-comment-mgmt', 'page', '/admin/mobile/comments', 'views/mobile-admin/CommentManagementView.vue', 40, 'y', 'Sys_Admin comment soft delete', NULL, NULL)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `menu_name` = VALUES(`menu_name`),
  `menu_code` = VALUES(`menu_code`),
  `menu_type` = VALUES(`menu_type`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `sort_order` = VALUES(`sort_order`),
  `status` = VALUES(`status`),
  `comments` = VALUES(`comments`),
  `update_user_id` = VALUES(`update_user_id`);
