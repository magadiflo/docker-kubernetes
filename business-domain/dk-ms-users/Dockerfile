FROM openjdk:17-jdk-alpine
WORKDIR /app/business-domain/dk-ms-users

COPY ./pom.xml /app
COPY ./business-domain/pom.xml /app/business-domain
COPY ./business-domain/dk-ms-users ./

RUN sed -i -e 's/\r$//' ./mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8001
CMD ["java", "-jar", "./target/dk-ms-users-0.0.1-SNAPSHOT.jar"]