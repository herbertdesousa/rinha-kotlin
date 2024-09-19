FROM openjdk:18-jdk-slim

WORKDIR /app

COPY app.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

EXPOSE 9393