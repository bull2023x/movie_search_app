# Stage 1: build with Maven + JDK17
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# copy just the pom first and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# now copy sources and build the fat-jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: run with just JRE
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# grab our packaged jar
COPY --from=build /app/target/sakila-api-0.0.1-SNAPSHOT.jar app.jar

# expose port and start
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

