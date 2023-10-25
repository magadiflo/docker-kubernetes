FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY ./target/*.jar ./app.jar
EXPOSE 8001
CMD ["java", "-jar", "app.jar"]