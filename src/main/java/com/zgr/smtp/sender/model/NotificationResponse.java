package com.zgr.smtp.sender.model;

import com.zgr.smtp.sender.enums.ResponseError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private String mtNum;
    private String id;
    private ResponseError error;
    private String extendedDescription;

    public boolean hasBody(){
        return this.getError() == null;
    }
}
