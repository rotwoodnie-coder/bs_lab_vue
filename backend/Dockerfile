# =============================
#  后端 Docker 多阶段构建
#  使用 maven:3.9-eclipse-temurin-11 编译，缩小最终镜像
# =============================

# --- 构建阶段 ---
FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn clean package -DskipTests -B

# --- 运行阶段 ---
FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

# 时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 非 root 用户运行
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8010

ENTRYPOINT ["java", \
    "-Xms512m", "-Xmx1024m", \
    "-XX:+UseG1GC", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/tmp/heapdump.hprof", \
    "-jar", "app.jar", \
    "--spring.profiles.active=${SPRING_PROFILE:prod}" \
]
