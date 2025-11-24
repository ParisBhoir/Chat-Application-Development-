FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar
COPY otel/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/opentelemetry-javaagent.jar \
  -Dotel.service.name=user-service \
  -Dotel.exporter.otlp.endpoint=http://otel-collector:4318"

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
