package com.zgr.mail.sender.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Action {
    private String title;
    private String action;
    private String options;
}
