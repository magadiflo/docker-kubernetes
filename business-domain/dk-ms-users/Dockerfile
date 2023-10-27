FROM openjdk:17-jdk-alpine
WORKDIR /app/business-domain/dk-ms-users

COPY ./pom.xml /app
COPY ./business-domain/pom.xml /app/business-domain
COPY ./business-domain/dk-ms-users/pom.xml ./
COPY ./business-domain/dk-ms-users/mvnw ./
COPY ./business-domain/dk-ms-users/.mvn ./.mvn

RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw dependency:go-offline

COPY ./business-domain/dk-ms-users/src ./src
RUN ./mvnw clean package -DskipTests

EXPOSE 8001
CMD ["java", "-jar", "./target/dk-ms-users-0.0.1-SNAPSHOT.jar"]