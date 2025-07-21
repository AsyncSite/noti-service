FROM eclipse-temurin:21-jre

WORKDIR /app

# 보안을 위한 non-root 사용자 생성
RUN groupadd -g 1001 appgroup && \
    useradd -r -u 1001 -g appgroup appuser

# 사전 빌드된 JAR 파일 복사
COPY --chown=appuser:appgroup build/libs/noti-service-0.0.1-SNAPSHOT.jar app.jar

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/actuator/health || exit 1

USER appuser
EXPOSE 8084

ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker}", "-jar", "app.jar"] 