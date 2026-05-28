FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/file-integrity-monitor-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
COPY files files

CMD ["java", "-jar", "app.jar"]