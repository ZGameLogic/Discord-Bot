FROM ubuntu:latest
LABEL authors="Ben Shabowski"

FROM arm64v8/openjdk:21-jdk-buster

WORKDIR /app
COPY /target/DiscordBot-1.0.jar /app/DiscordBot-1.0.0.jar

EXPOSE 2000

CMD ["java", "-jar", "-Dspring.profiles.active=cluster", "DiscordBot-1.0.0.jar"]
