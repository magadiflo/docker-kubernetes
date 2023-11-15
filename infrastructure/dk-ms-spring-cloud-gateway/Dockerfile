FROM openjdk:17-jdk-alpine AS builder
WORKDIR /app/infrastructure/dk-ms-spring-cloud-gateway
COPY ./pom.xml /app
COPY ./infrastructure/pom.xml /app/infrastructure
COPY ./infrastructure/dk-ms-spring-cloud-gateway/pom.xml ./
COPY ./infrastructure/dk-ms-spring-cloud-gateway/mvnw ./
COPY ./infrastructure/dk-ms-spring-cloud-gateway/.mvn ./.mvn
RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline
COPY ./infrastructure/dk-ms-spring-cloud-gateway/src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/infrastructure/dk-ms-spring-cloud-gateway/target/*.jar ./app.jar
EXPOSE 8002
CMD ["java", "-jar", "app.jar"]