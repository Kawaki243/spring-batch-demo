FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/springBatchDemo-0.0.1-SNAPSHOT springbatch-v1.0.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "springbatch-v1.0.jar"]