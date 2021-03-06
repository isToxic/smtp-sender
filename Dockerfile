FROM java:8-jre

ADD ./build/libs/*.jar /app/
ADD ./config/* /app/config/
WORKDIR /app
RUN ls config
CMD ["java", "-Xmx200m", "-jar", "/app/smtp-sender-0.0.1-SNAPSHOT.jar"]

EXPOSE 9998