# 智慧医疗后端镜像
# 构建：docker build -t smart-medical:latest .
# 运行：docker run -p 8080:8080 --env-file .env smart-medical:latest

# 构建期参数：默认值与现有一致，可用 --build-arg 覆盖。
# 注意：密钥禁止走 ARG / ENV（ARG 会出现在 docker history，ENV 会固化进镜像层），
#       运行期密钥一律通过 --env-file / compose env_file 注入。
ARG JDK_IMAGE=eclipse-temurin:17-jdk-alpine
ARG JRE_IMAGE=eclipse-temurin:17-jre-alpine
ARG MAVEN_ARGS="clean package -DskipTests"

FROM ${JDK_IMAGE} AS builder

WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src ./src

# 跨 stage 复用需在本 stage 内重新声明
ARG MAVEN_ARGS
RUN chmod +x mvnw && ./mvnw ${MAVEN_ARGS}

FROM ${JRE_IMAGE}

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
