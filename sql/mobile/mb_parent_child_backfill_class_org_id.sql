-- ============================================================

-- 回填历史 pending 绑定申请的 class_org_id（便于教师待审核列表匹配）

-- 前置：mb_parent_child.class_org_id 列已存在

-- ============================================================



UPDATE `mb_parent_child` b

INNER JOIN `sys_user` s ON s.`user_id` = b.`child_user_id`

SET b.`class_org_id` = s.`user_org_id`

WHERE (b.`class_org_id` IS NULL OR b.`class_org_id` = '')

  AND s.`user_org_id` IS NOT NULL

  AND s.`user_org_id` <> '';

