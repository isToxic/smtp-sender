package com.zgr.smtp.sender.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum NotificationResponseError {
    UNKNOWN(1, "Неизвестная ошибка."),
    ABSENT_SUBSCRIBER(2, "Абонент недоступен. Например, из-за уровня сигнала, выключения телефона."),
    CALL_BARRED(3, "У абонента включен запрет на прием сообщений или абонент временно заблокирован оператором (например, в связи с отрицательным балансом)."),
    FAILURE(4, "Внутренняя ошибка оператора. Например, ошибка маршрутизации, ошибка коммутатора (внутренняя ошибка передачи данных) и т.п."),
    MEMORY_CAPACITY_EXCEEDED(5, "Память телефона абонента переполнена."),
    TELESERVICE_NOT_PROVISIONED(6, "Сервис коротких сообщений не предоставляется. Например, услуга SMS не подключена или временно заблокирована, либо аппарат абонента не поддерживает обмен короткими текстовыми сообщениями."),
    TIMEOUT(7, "Истекло время ожидания ответа от SMS-центра или от аппарата абонента."),
    UNKNOWN_SUBSCRIBER(8, "Номер телефона не существует (или не обслуживается, не распознан оператором, абонент не зарегистрирован в сети оператора и т.п.)."),
    DUPLICATED(9, "Сообщение было отброшено платформой, так как сработал механизмом отсечения дубликатов SMS сообщений."),
    FILTERED(10, "Сообщение было отброшено Сервис-Провайдером, так как сработал один из фильтров SMS сообщений. Например, сработал спам-фильтр."),
    OPER_BLACKLISTED(12, "Номер абонента находится в чёрном списке оператора."),
    OPER_INVSRCADDR(13, "Отправка сообщения с незарегистрированного у оператора имени отправителя."),
    OPER_SPAMFILTERED(14, "На стороне оператора сработал СПАМ-фильтр по тексту сообщения.");

    private final int code;
    private final String description;

    public static String getDescriptionByCode(int code) {
        return Arrays.stream(NotificationResponseError.values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .description;
    }
}
