FROM eclipse-temurin:17-jre-alpine
COPY target/bookstore-management-tdd-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","/app.jar"]