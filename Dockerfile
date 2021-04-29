FROM java:8-jre

ADD ./build/libs/*.jar /app/
ADD ./config /app/

CMD ["java", "-Xmx200m", "-jar", "/app/smtp-sender-0.0.1-SNAPSHOT.jar"]

EXPOSE 9998