package com.huanhong.decathlonstb.event;

import java.text.SimpleDateFormat;

/**
 * Created by done on 2018/3/9.
 */

public class TenMinutesEvent implements IEvent {

    private Config config;
    protected SimpleDateFormat hhmm = new SimpleDateFormat("HH:mm");

    public TenMinutesEvent(Config config) {
        this.config = config;
    }

    @Override
    public boolean isOk() {
        String hour_minute = hhmm.format(System.currentTimeMillis());
        return hour_minute.substring(hour_minute.length() - 1, hour_minute.length()).equals("0");
    }

    @Override
    public void ok() {
    }

    @Override
    public void end() {

    }

    @Override
    public Config getConfig() {
        return config;
    }
}