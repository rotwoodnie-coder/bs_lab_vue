# =============================
#  移动端前端 Docker 多阶段构建
# =============================

# --- 构建阶段 ---
FROM node:20-alpine AS build

RUN corepack enable && corepack prepare pnpm@10.33.2 --activate

WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm build:lan

# --- 运行阶段 ---
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://localhost/ || exit 1
