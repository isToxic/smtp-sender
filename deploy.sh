#!/bin/bash

chmod +x gradlew
# Сборка и запуск контейнера
docker build -t smtp-sender .
docker rm -f smtp-sender || true
docker run -d --name smtp-sender smtp-sender

# Запуск без docker
#"$JAVA_HOME"/bin/java -jar -Xms256m -Xmx2048m ./smtp-sender-0.0.1-SNAPSHOT.jar

exit 0