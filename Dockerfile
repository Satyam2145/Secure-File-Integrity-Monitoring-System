FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/file-integrity-monitor-1.0-SNAPSHOT.jar app.jar
COPY files files

CMD ["java", "-jar", "app.jar"]