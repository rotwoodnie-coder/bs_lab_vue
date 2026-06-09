# 系统登录账号清单

> 生成：2026-06-09 10:36 · 数据源：`bs_exp_vue.sys_user`

密码字段在库中为 **BCrypt 哈希**；下表「可用明文密码」为脚本对常见测试口令校验结果，**无法反推则留空**。

| 登录名 | 姓名 | 角色 | 状态 | 手机 | user_id | 可用明文密码 | 密码哈希（前缀） |
|--------|------|------|------|------|---------|--------------|------------------|
| `13800138001` | Test Parent | Parent | t | 13800138001 | `dev-parent-pending-01` | `123456` | `$2a$10$Uv9Pr8X8nPj6/HLy3/eaku…` |
| `13812345678` | 杨丽娟 | Parent | t | 13812345678 | `b5182e1323b9421992784ff49e25f70a` | `12345678` | `$2a$10$KADAZ2LeikGwpuVSFB3DKu…` |
| `18918902651` | 张胜利 | Parent | t | 18918902651 | `53267bd530db46eb82495f175de006b3` | `12345678` | `$2a$10$RRBoL1sUJKgBcPEETk1shu…` |
| `yuanf` | 袁飞 | Researcher | y |  | `ab786f33c05b4df198079458365688df` | `123456` | `$2a$10$KLrmDnFad/Wf5QFN.l2GUu…` |
| `bhy_admin` | 宝华曜管理员 | School_Admin | y | 13912345678 | `0601ff00334343d49e116055fb3be56a` | `123456` | `$2a$10$aGk3Y9ySq5Zp/RgHVzTxru…` |
| `zhangxm` | 张小明 | Student | y |  | `a6ef4dd7298d4914ae58d2dccc136ae8` | `123456` | `$2a$10$Uv9Pr8X8nPj6/HLy3/eaku…` |
| `admin` | admin | Sys_Admin | y | 13601360136 | `Sys_Admin` | `123456` | `$2a$10$h0otAZ2qH0X2TLx6WUnm6u…` |
| `gaoy` | 高研 | Teacher | y |  | `9a77dde99aaa4b689bd69ddfdedb6cb0` | `123456` | `$2a$10$fWlw2.0uXhkJcyzHZ7fWMO…` |
| `liangwy` | 梁文怡 | Teacher | y |  | `220f1898089145dd92e0c0f351e9d9bb` | `123456` | `$2a$10$ftFsQkY9t8gmATezGVH4Re…` |

## 说明

- **登录接口**：`POST /api/auth/login`（管理端）、`POST /api/mobile/auth/login`（移动端）
- **管理端开发**：`frontend` Vite 代理 `/api` → `127.0.0.1:8011`（勿去掉 `/api` 前缀）
- **移动端开发**：推荐 `pnpm build:lan` 后访问 `http://本机IP:8011/m/#/login`；Vite 5174 模式走 `/api` 代理
- **203 等旧环境**：若 `/api/mobile/auth/login` 返回 404，需部署当前 `exp-backend` 包并重启
- **新建用户默认密码**：管理端创建用户时若未填密码，后端默认写入 `123456`（见 `UserServiceImpl`）。
- **admin / test1 / test2**：`UserServiceImpl` 对 `123456` 有硬编码兜底（管理端改密场景）；登录接口 `AuthServiceImpl` **仅** BCrypt 校验。
- **家长账号**：移动端注册时自行设置密码（手机号即 `login_name`）。
- **status**：`y` 启用、`t` 待激活（注册待审）、`n` 停用。
- 本文档含数据库账号信息，**仅限内网开发联调**，勿提交至公开仓库或外发。
