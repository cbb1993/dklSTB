package com.huanhong.decathlonstb.event;

import com.huanhong.decathlonstb.db.DBManager;

/**
 * Created by done on 2018/3/9.
 */

public interface IEvent {

    boolean isOk();

    void ok();

    void end();

    Config getConfig();

    public class Config {
        public String url;
        public String store;
        public DBManager db;

        public Config(String url, String store, DBManager db) {
            this.url = url;
            this.store = store;
            this.db = db;
        }
    }
}