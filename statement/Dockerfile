FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY statement/pom.xml ./statement/
COPY statement/src ./statement/src/

RUN mvn -f statement/pom.xml clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=builder /app/statement/target/*.jar statement.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "statement.jar"]