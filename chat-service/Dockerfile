FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/chat-service-0.0.1-SNAPSHOT.jar app.jar
COPY otel/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/opentelemetry-javaagent.jar \
  -Dotel.service.name=chat-service \
  -Dotel.exporter.otlp.endpoint=http://otel-collector:4318"


EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
