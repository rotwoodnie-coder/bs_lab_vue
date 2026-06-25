-- 可选：将 is_public 默认值与历史脏数据统一为 y/n
ALTER TABLE `data_file`
  MODIFY COLUMN `is_public` varchar(32) NOT NULL DEFAULT 'n' COMMENT '是否公开（y=公开，n=不公开）';

UPDATE `data_file` SET `is_public` = 'n' WHERE `is_public` IN ('0', '');
