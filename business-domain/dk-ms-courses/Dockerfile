FROM openjdk:17-jdk-alpine AS builder
WORKDIR /app/business-domain/dk-ms-courses
COPY ./pom.xml /app
COPY ./business-domain/pom.xml /app/business-domain
COPY ./business-domain/dk-ms-courses/pom.xml ./
COPY ./business-domain/dk-ms-courses/mvnw ./
COPY ./business-domain/dk-ms-courses/.mvn ./.mvn
RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline
COPY ./business-domain/dk-ms-courses/src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/business-domain/dk-ms-courses/target/*.jar ./app.jar
EXPOSE 8002
CMD ["java", "-jar", "app.jar"]