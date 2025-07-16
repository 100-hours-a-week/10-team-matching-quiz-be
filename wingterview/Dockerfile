FROM openjdk:21-jdk-slim

WORKDIR /app

# JAR 복사
COPY build/libs/*.jar app.jar

# secret 설정파일은 jar 외부에 존재하지만, Spring이 읽을 수 있도록 전달 필요
COPY src/main/resources/application-secret.yml /app/application-secret.yml

# 포트 오픈 (선택)
EXPOSE 8080

# 실행 시 application-secret.yml을 명시적으로 설정
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.additional-location=application-secret.yml"]