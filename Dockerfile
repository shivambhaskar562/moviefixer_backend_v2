# Stage 1: Build with Maven and JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven wrapper files and configuration
COPY mvnw .
COPY .mvn .mvn

# Copy pom.xml and resolve dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application with a lightweight JDK 21
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/MovieFixer-1.0.jar .

# Expose port for the application
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "MovieFixer-1.0.jar"]
