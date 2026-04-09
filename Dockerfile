# Base image
FROM eclipse-temurin:25.0.2_10-jdk

# Working directory
WORKDIR /app

# Copy JAR
COPY target/*.jar app.jar

# Environment variables
ENV USER_NAME=Docker_Malak_Yassin
ENV ID=Docker_55_3546

# Expose port
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]