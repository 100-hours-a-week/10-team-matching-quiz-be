FROM openjdk:21-jdk-slim
WORKDIR /app

# JAR 파일과 설정 파일을 모두 복사
COPY build/libs/wingterview-0.0.1-SNAPSHOT.jar app.jar

COPY application-secret.yml application-secret.yml

ENTRYPOINT ["java", "-jar", "app.jar"]
