package com.zgr.smtp.sender.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PushContent {
    private String contentUrl;
    private String contentCategory;
    private List<Action> actions;
}
