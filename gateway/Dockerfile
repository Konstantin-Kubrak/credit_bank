FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY gateway/pom.xml ./gateway/
COPY gateway/src ./gateway/src/

RUN mvn -f gateway/pom.xml clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=builder /app/gateway/target/*.jar gateway.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "gateway.jar"]