-- ============================================================
-- 评论软删审计字段（已有 mb_comment 表时执行）
-- 关联：docs/mobile-product-spec.md §4.2（9.3）
-- ============================================================

ALTER TABLE `mb_comment`
  ADD COLUMN `deleted_by` varchar(32) DEFAULT NULL COMMENT 'soft delete by user_id' AFTER `status`,
  ADD COLUMN `deleted_time` datetime DEFAULT NULL COMMENT 'soft delete time' AFTER `deleted_by`;
