FROM openjdk:21-jdk-slim
WORKDIR /app

# 애플리케이션 JAR 복사
COPY build/libs/*.jar app.jar

# 시크릿 설정 파일 복사
COPY src/main/resources/application-secret.yml ./application-secret.yml

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/application.yml,classpath:/application-secret.yml"]
