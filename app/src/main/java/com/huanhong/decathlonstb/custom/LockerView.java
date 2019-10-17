package com.huanhong.decathlonstb.custom;

import com.huanhong.decathlonstb.db.DBManager;

/**
 * Created by done on 2018/3/9.
 */

public interface LockerView extends DBManager.OnDbListener {
    void lock();

    void unlock();

    void onDestroy();

    void httpState(boolean b);
}
