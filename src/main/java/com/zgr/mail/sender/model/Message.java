package com.zgr.mail.sender.model;

import com.zgr.mail.sender.enums.MessageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {
    private MessageType type;
    private com.zgr.mail.sender.model.Data data;
}
