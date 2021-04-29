Сервис обработки почты
=

Системные требования
-
- Java 8
- Прописана переменная окружения $JAVA_HOME с путём хранения java


Инструкция по установке
- 
Порядок установки:
1) Настройка переменных окружения
2) Запуск приложения

###Запуск приложения в Docker (скрипт deploy.sh в архиве):
1) Запустить скрипт: ./deploy.sh

###Запуск приложения без docker:
1) закомментировать пункт сборки и установки контейнера в deploy.sh
```shell
docker build -t smtp-sender .
docker rm -f smtp-sender || true
docker run -d --name smtp-sender smtp-sender
```   
2)Раскомментировать пункт
```shell
"$JAVA_HOME"/bin/java -jar -Xms256m -Xmx2048m ./smtp-sender-0.0.1-SNAPSHOT.jar
```
3) Сохранить и запустить deploy.sh

Описание работы
-
Данный сервис подключается по протоколу IMAP к настроенному почтовому серверу и получает оттуда письма.
При получении нового письма используя его данные, (номер берется из адреса email, текст сообщения из тела письма) происходит отправка по сценарию push->sms с использованием rest-клиента.


Получение запросов на интеграцию
-
Каждые polling-ms миллисекунд Сервис выполняет запрос на получение списка писем в объёме max-messages-per-poll для обработки.

Процесс обработки пакета сообщений
-
- Сервис производит валидацию письма, что оно имеет вид 79101230000@XXX.
- Сервис подготавливает запрос, для генерации каскада push->sms. (title = subject, text = body).
- Сервис производит отправку сообщения с использованием rest-клиента.

Получение статуса интеграции
-
По итогу обработки, письма перекладываются в директории folders.success или folders.error в соответствии с результатом обработки.

Настройки сервиса:
-
Настройки сервиса предоставлены в виде yaml файла.

### Пример и описание настроек

```yaml
mail-sender:
  connect-timeout-millis: 10000       // Таймаут подключения при отправке сообщений
  request-timeout-millis: 10000       // Таймаут запроса при отправке сообщений
  send:
    login: login                      // Логин в системе адресата
    password: pass                    // Пароль в системе адресата
    ttl: 60                           // Время жизни сообщения
    ttlUnit: MINUTES                  // Единицы измерения ttl
    url: htttp://send.to              // Url для отправки сообщений
    send-state: READ
    service-numbers:
      sms: number                     // Сервисный номер для отправки смс
      push: num                       // Сервисный номер для отправки пушей
  mail:
    user-name: all_mail               // Логин подключения для почтового сервера
    password: C6YQQkHOKLNjIT26        // Пароль подключения для почтового сервера
    host: 10.241.0.244                // Хост подключения для почтового сервера
    port: 143                         // Порт подключения для почтового сервера
    timeout: 20000                    // Таймаут подключения
    polling-ms: 10                    // Частота вычитки сообщений
    connection-poll-size: 4           // Размер пула подключений
    max-poll-size: 1                  // Максимальный размер пула обработки сообщений
    max-messages-per-poll: 1          // Максимальный размер пачки обработки сообщений
    debug: false                      // debug mode javax.mail
    partialfetch: true
    folders:
      receive: INBOX                  // Директория для вычитки сообщений
      success: COMPLETE               // Директория для успешно обработанных сообщений
      error: ERROR                    // Директория для не успешно обработанных сообщений
# логировние: путь, уровни логирования
logging:
  #file: /var/log/mail-sender
  level:
    org.springframework: info
    org.apache.http: info
    com.zgr: info
    org.springframework.integration.handler.LoggingHandler: off
  file:
    name: /opt/smtp-sender/logs
  logback:
    rollingpolicy:
      max-file-size: 20MB
      max-history: 30
```