package com.huanhong.decathlonstb.event;

import com.huanhong.decathlonstb.db.DBManager;

import java.text.SimpleDateFormat;

/**
 * Created by done on 2018/3/10.
 */

public class EventNewDay implements IEvent {

    private DBManager db;
    private SimpleDateFormat hhmm = new SimpleDateFormat("HH:mm");

    public EventNewDay(DBManager db) {
        this.db = db;
    }

    @Override
    public boolean isOk() {
        return hhmm.format(System.currentTimeMillis()).equals("00:00");
    }

    @Override
    public void ok() {
        db.newDay();
    }

    @Override
    public void end() {

    }

    @Override
    public Config getConfig() {
        return null;
    }
}