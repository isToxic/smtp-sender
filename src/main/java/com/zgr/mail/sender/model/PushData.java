package com.zgr.mail.sender.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@lombok.Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PushData extends Data {
    private String title;
    private com.zgr.mail.sender.model.PushContent content;
    private com.zgr.mail.sender.model.CustomPayload customPayload;
}
