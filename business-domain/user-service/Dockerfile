FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY ./ ./
RUN sed -i -e 's/\r$//' ./mvnw  && ./mvnw clean package -DskipTests
EXPOSE 8001
CMD ["java", "-jar", "./target/user-service-0.0.1-SNAPSHOT.jar"]
