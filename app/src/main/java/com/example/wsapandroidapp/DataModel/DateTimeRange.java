package com.example.wsapandroidapp.DataModel;

public class DateTimeRange {

    private long startDateTime, endDateTime;

    public DateTimeRange() {
    }

    public DateTimeRange(long startDateTime, long endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public long getStartDateTime() {
        return startDateTime;
    }

    public long getEndDateTime() {
        return endDateTime;
    }
}
