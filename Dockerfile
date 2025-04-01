FROM ubuntu:latest
LABEL authors="Ben Shabowski"

FROM arm64v8/openjdk:21-jdk-buster

WORKDIR /app
COPY /target/DiscordBot-1.0.jar /app/DiscordBot-1.0.0.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-cert}

CMD ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "DiscordBot-1.0.0.jar"]
