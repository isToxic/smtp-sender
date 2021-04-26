package com.zgr.mail.sender.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ResponseError {
    UNAVAILABLE(1, "Service is unavailable"),
    INVALID_IP_ADRESS(2, "Invalid IP-address"),
    TO_MANY_CONNECTIONS(3, "Too many connections"),
    INVALID_REQUEST(4, "Invalid request"),
    INVALID_LOGIN(5, "Invalid login"),
    INVALID_PASSWORD(6, "Invalid password"),
    NO_SERVICE_NUMBER(7, "\"serviceNumber\" is not defined"),
    NO_DEST_ADDR(8, "\"destAddr\" is not correct"),
    WRONG_MESSAGE_TYPE(9, "Message type is not correct"),
    DUPLICATE(10, "Prohibited sending duplicates"),
    INVALID_TTL(11, "Invalid TTL"),
    INTERNAL_ERROR(100, "Internal error");

    private final int code;
    private final String description;

    @JsonCreator
    public static ResponseError forValues(@JsonProperty("code") int code,
                                          @JsonProperty("description") String description) {
        for (ResponseError error : values()) {
            if (
                    (error.getCode() == code) && error.getDescription().equals(description)) {
                return error;
            }
        }
        return null;
    }
}
