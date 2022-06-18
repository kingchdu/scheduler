package com.demo.scheduler.constants;

public enum ScheduleTypeEnum {

    /**
     * schedule by crontab expression
     */
    CRON("CRON", 0),

    /**
     * schedule by fixed rate (in seconds)
     */
    FIX_RATE("FIX_RATE", 1);

    private String title;

    private int code;

    ScheduleTypeEnum(String title, Integer code) {
        this.title = title;
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public Integer getCode() {
        return code;
    }

}
