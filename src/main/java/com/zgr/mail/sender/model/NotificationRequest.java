package com.zgr.mail.sender.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {
    private String login;
    private String password;
    private boolean useTimeDiff;
    private String id;
    private com.zgr.mail.sender.model.ScheduleInfo scheduleInfo;
    private String destAddr;
    private Message message;
    private CascadeChainLink cascadeChainLink;
}


