# 智慧医疗后端镜像
# 构建：docker build -t smart-medical:latest .
# 运行：docker run -p 8080:8080 --env-file .env smart-medical:latest

# 构建期参数：默认值与现有一致，可用 --build-arg 覆盖。
# 注意：密钥禁止走 ARG / ENV（ARG 会出现在 docker history，ENV 会固化进镜像层），
#       运行期密钥一律通过 --env-file / compose env_file 注入。

# syntax=docker/dockerfile:1

ARG JDK_IMAGE=eclipse-temurin:17-jdk-alpine
ARG JRE_IMAGE=eclipse-temurin:17-jre-alpine
ARG MAVEN_ARGS="clean package -DskipTests"

FROM ${JDK_IMAGE} AS builder

WORKDIR /app

# 配置 Maven 使用阿里云镜像，并设置重试参数
RUN mkdir -p /root/.m2 \
    && echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml \
    && echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /root/.m2/settings.xml \
    && echo '  <mirrors>' >> /root/.m2/settings.xml \
    && echo '    <mirror>' >> /root/.m2/settings.xml \
    && echo '      <id>aliyunmaven</id>' >> /root/.m2/settings.xml \
    && echo '      <mirrorOf>*</mirrorOf>' >> /root/.m2/settings.xml \
    && echo '      <name>阿里云公共仓库</name>' >> /root/.m2/settings.xml \
    && echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml \
    && echo '    </mirror>' >> /root/.m2/settings.xml \
    && echo '  </mirrors>' >> /root/.m2/settings.xml \
    && echo '</settings>' >> /root/.m2/settings.xml

# 复制 Maven Wrapper 和 pom.xml（利用缓存）
COPY .mvn .mvn
COPY mvnw pom.xml ./

# 先下载依赖（使用缓存挂载 .m2，并增加重试参数）
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && \
    ./mvnw dependency:go-offline -B \
    -Dmaven.wagon.http.retryHandler.count=3 \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

# 再复制源码并执行构建
COPY src ./src

ARG MAVEN_ARGS
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw ${MAVEN_ARGS} -B \
    -Dmaven.wagon.http.retryHandler.count=3 \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120

FROM ${JRE_IMAGE}

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
