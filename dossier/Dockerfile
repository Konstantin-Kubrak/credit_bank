FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY dossier/pom.xml ./dossier/
COPY dossier/src ./dossier/src/

RUN mvn -f dossier/pom.xml clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=builder /app/dossier/target/*.jar dossier.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "dossier.jar"]