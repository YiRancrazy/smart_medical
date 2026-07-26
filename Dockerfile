# 智慧医疗后端镜像
# 构建：docker build -t smart-medical:latest .
# 运行：docker run -p 8080:8080 --env-file .env smart-medical:latest
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src ./src

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
