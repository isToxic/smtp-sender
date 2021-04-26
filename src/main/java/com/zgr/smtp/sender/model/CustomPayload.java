package com.zgr.smtp.sender.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomPayload {
    private String deeplink;
}
