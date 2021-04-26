package com.zgr.smtp.sender.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@lombok.Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PushData extends Data {
    private String title;
    private PushContent content;
    private CustomPayload customPayload;
}
