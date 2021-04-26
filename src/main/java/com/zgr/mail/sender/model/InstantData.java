package com.zgr.mail.sender.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InstantData {
    private String text;
    private String imageURL;
    private String caption;
    private String action;
    private String documentURL;
    private String documentName;
    private String audioURL;
    private String videoURL;
    private String videoName;
}
