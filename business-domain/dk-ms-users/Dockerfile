ARG MICROSERVICE_NAME=dk-ms-users

FROM openjdk:17-jdk-alpine AS builder
ARG MICROSERVICE_NAME
WORKDIR /app/business-domain/${MICROSERVICE_NAME}
COPY ./pom.xml /app
COPY ./business-domain/pom.xml /app/business-domain
COPY ./business-domain/${MICROSERVICE_NAME}/pom.xml ./
COPY ./business-domain/${MICROSERVICE_NAME}/mvnw ./
COPY ./business-domain/${MICROSERVICE_NAME}/.mvn ./.mvn
RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline
COPY ./business-domain/${MICROSERVICE_NAME}/src ./src
RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine
ARG MICROSERVICE_NAME
ARG HOST_PORT=8001
WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/business-domain/${MICROSERVICE_NAME}/target/*.jar ./app.jar
ENV CONTAINER_PORT=8001
EXPOSE ${HOST_PORT}
CMD ["java", "-jar", "app.jar"]