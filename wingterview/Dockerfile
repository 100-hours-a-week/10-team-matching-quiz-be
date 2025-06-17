FROM openjdk:21-jdk-slim
WORKDIR /app

# JAR 파일을 컨테이너에 복사
COPY build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
