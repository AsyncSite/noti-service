# Dockerfile for noti-service
# Requires JAR to be built locally first: ./gradlew clean build
# This approach avoids authentication issues during Docker build

FROM amazoncorretto:21
WORKDIR /app

# Create non-root user for security and logs directory
RUN useradd -m -u 1001 appuser && \
    mkdir -p /app/logs && \
    chown -R appuser:appuser /app && \
    chmod 755 /app/logs

# Copy pre-built JAR file
# Spring Boot generates: noti-service-0.0.1-SNAPSHOT.jar
COPY --chown=appuser:appuser build/libs/noti-service-*.jar app.jar

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8089

# Set JVM options for container environment with UTF-8 encoding
ENV JAVA_OPTS="-Xmx512m -Xms256m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]