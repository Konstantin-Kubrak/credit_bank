FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY deal/pom.xml ./deal/
COPY deal/src ./deal/src/

RUN mvn -f deal/pom.xml clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=builder /app/deal/target/*.jar deal.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "deal.jar"]