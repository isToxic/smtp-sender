package com.zgr.smtp.sender.model;

import com.zgr.smtp.sender.enums.MessageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {
    private MessageType type;
    private com.zgr.smtp.sender.model.Data data;
}
