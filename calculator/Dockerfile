FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY calculator/pom.xml ./calculator/
COPY calculator/src ./calculator/src/

RUN mvn -f calculator/pom.xml clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=builder /app/calculator/target/*.jar calculator.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "calculator.jar"]