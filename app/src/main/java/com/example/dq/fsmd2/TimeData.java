package com.example.dq.fsmd2;

import java.util.Date;

/**
 * Created by DQ on 12/16/2017.
 */

public class TimeData {
    private Date date;

    public TimeData() {
        date = null;
    }

    public TimeData(Date date) {
        this.date = date;
    }

    public void setDate(Date date) { this.date = date; }
    public Date getDate() { return date; }
}
