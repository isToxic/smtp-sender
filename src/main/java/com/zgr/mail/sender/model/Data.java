package com.zgr.mail.sender.model;

import lombok.experimental.SuperBuilder;

@SuperBuilder
@lombok.Data
public class Data {
    private String text;
    private String serviceNumber;
    private int ttl;
    private String ttlUnit;
}
