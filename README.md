# 后台管理系统基础架构

## 技术栈
- 前端：Vue 3 + Vite + Vue Router + Pinia + Element Plus
- 后端：Spring Boot 2.7 + Hibernate/JPA + MySQL 5.7
- JDK：1.8

## 目录结构
- `frontend/`：前端工程
- `backend/`：后端工程
- `sql/`：现有数据库脚本

## 启动方式
### 前端
```bash
cd frontend
npm install
npm run dev
```

### 后端
```bash
cd backend
mvn spring-boot:run
```

## 数据库说明
当前后端默认连接：
- 数据库名：`bs_exp_data`
- 用户名：`root`
- 密码：`root`

请根据你的本地环境修改 `backend/src/main/resources/application.yml`。
