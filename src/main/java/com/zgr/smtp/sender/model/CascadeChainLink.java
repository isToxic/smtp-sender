package com.zgr.smtp.sender.model;

import com.zgr.smtp.sender.enums.RepeatSendState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CascadeChainLink {
    private RepeatSendState state;
    private Message message;
    private CascadeChainLink nextLink;
}
