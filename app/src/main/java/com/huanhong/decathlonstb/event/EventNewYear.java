package com.huanhong.decathlonstb.event;

import com.huanhong.decathlonstb.db.DBManager;

import java.text.SimpleDateFormat;

/**
 * Created by done on 2018/3/10.
 */

public class EventNewYear implements IEvent {

    private SimpleDateFormat hhmm = new SimpleDateFormat("MM-dd HH:mm");
    private DBManager db;

    public EventNewYear(DBManager db) {
        this.db = db;
    }

    @Override
    public boolean isOk() {
        return hhmm.format(System.currentTimeMillis()).equals("01-01 00:00");
    }

    @Override
    public void ok() {
        db.newYear();
    }

    @Override
    public void end() {

    }

    @Override
    public Config getConfig() {
        return null;
    }
}