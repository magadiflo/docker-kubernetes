ARG JDK_VERSION=21-jdk-alpine
ARG JRE_VERSION=21-jre-alpine

FROM eclipse-temurin:${JDK_VERSION} AS dependencies
WORKDIR /app
COPY ./mvnw ./
COPY ./.mvn ./.mvn
COPY ./pom.xml ./
RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline
COPY ./src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:${JRE_VERSION} AS builder
WORKDIR /app
COPY --from=dependencies /app/target/*.jar ./app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:${JRE_VERSION} AS runner
WORKDIR /app
RUN mkdir ./logs
COPY --from=builder /app/dependencies ./
COPY --from=builder /app/spring-boot-loader ./
COPY --from=builder /app/snapshot-dependencies ./
COPY --from=builder /app/application ./

ENV CONTAINER_PORT=9000
EXPOSE ${CONTAINER_PORT}
CMD ["java", "org.springframework.boot.loader.launch.JarLauncher"]
