package com.zgr.mail.sender.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum NotificationResponseStatus {
    DELIVERED(2), UNDELIVERED(5), OPEN(9);

    private final int code;

    public static NotificationResponseStatus getByCode(int code) {
        return Arrays.stream(NotificationResponseStatus.values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
