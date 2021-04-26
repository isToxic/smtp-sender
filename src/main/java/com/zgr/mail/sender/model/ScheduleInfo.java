package com.zgr.mail.sender.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScheduleInfo {
    private String timeBegin;
    private String timeEnd;
    private String weekdaysSchedule;
    private String deadline;
}
