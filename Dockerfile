# ==== Bygg backend ====
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /opt/app

# Kopiera Maven wrapper + config
COPY Multiplayer-Backend/.mvn .mvn
COPY Multiplayer-Backend/mvnw .
COPY Multiplayer-Backend/pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Kopiera källkod
COPY Multiplayer-Backend/src ./src

# Kopiera frontend (html/js/css) till Spring Boot static/
COPY Multiplayer-Frontend ./src/main/resources/static

# Bygg jar
RUN ./mvnw clean package -DskipTests

# ==== Runtime ====
FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app
EXPOSE 8080

COPY --from=builder /opt/app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
