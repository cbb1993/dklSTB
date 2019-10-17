package com.huanhong.decathlonstb.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.huanhong.decathlonstb.model.ModelComment;
import com.huanhong.decathlonstb.util.StoreAmout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBManager {

    String TAG = getClass().getSimpleName();
    private final String TABLE_SETTING = "decathlongs_set";
    private final String TABLE_DATA = "decathlongs_data";
    private final String TABLE_CACHES = "decathlongs_caches";
    private final String TABLE_SEND = "decathlongs_watchs";

    private Database idata;
    public SQLiteDatabase db;
    private ExecutorService singleThreadExecutor = null;
    private List<OnDbListener> dbManagerList = null;
    private Handler handler = null;

    public DBManager(Context context) {
        dbManagerList = new ArrayList<>();
        if (idata == null) {
            idata = new Database(context);
        }
        if (db == null) {
            db = idata.getWritableDatabase();
        }
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
    }

    protected int getTableInt(String table, String key) {
        int v = 0;
        Cursor c = db.query(table, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            try {
                int ci = c.getColumnIndex(key);
                if (ci != -1)
                    v = c.getInt(ci);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        c.close();
        return v;
    }

    protected String getTableString(String table, String key) {
        String v = null;
        Cursor c = db.query(table, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            try {
                int ci = c.getColumnIndex(key);
                if (ci != -1)
                    v = c.getString(ci);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        c.close();
        return v;
    }

    protected int getDataInt(String key) {
        return getTableInt(TABLE_DATA, key);
    }

    public String getSettingString(String key) {
        return getTableString(TABLE_SETTING, key);
    }

    public int getYearsCount() {
        return getDataInt("yc");
    }

    public int getDaysCount() {
        return getDataInt("dc");
    }

    public int getYearsGoodCount() {
        return getDataInt("ygc");
    }

    public int getDaysGoodCount() {
        return getDataInt("dgc");
    }

    public int getLastId() {
        return getDataInt("lastid");
    }

    public StoreAmout getCacheCount() {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
        Cursor c = db.query(TABLE_CACHES, null, null, null, null, null, null);
        StoreAmout count = new StoreAmout();
        String todaydate = sd.format(System.currentTimeMillis());
        if (c != null) {
            while (c.moveToNext()) {
                int type = c.getInt(c.getColumnIndex("type"));
                long time = Long.parseLong(c.getString(c.getColumnIndex("time")));
                String date = sd.format(time);
                count.year_count++;
                if (type == 1) {
                    count.year_count_score++;
                }
                if (date.equals(todaydate)) {
                    count.to_days_count++;
                    if (type == 1) {
                        count.to_days_count_score++;
                    }
                }
            }
            c.close();
        }
        return count;
    }

    public String getStore() {
        return getSettingString("store");
    }
    public String getStoreNo() {
        return getStore().split("##")[2].split("-")[0];
    }

    public String getIp() {
        return getSettingString("ip");
    }

    public String getDate() {
        return getSettingString("date");
    }

    public String getYear() {
        return getSettingString("year");
    }

    public String getHttpUrl() {
        return getSettingString("http");
    }

    public void checkDate() {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
        if (!date.equals(getDate())) {
            newDay();
        }
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        if (!year.equals(getYear())) {
            newYear();
        }
    }

    public synchronized void queryCaches(final int max, final OnDoneListener<List<ModelComment>> listener) {
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<ModelComment> data = new ArrayList<>();
                Cursor c = db.query(TABLE_CACHES, null, null, null, null, null, null);
                int i = 0;
                if (c != null && c.getCount() > 0) {
                    while (c.moveToNext()) {
                        if (i > max)
                            break;
                        ModelComment comment = new ModelComment();
                        comment.id = c.getInt(c.getColumnIndex("id"));
                        comment.type = c.getInt(c.getColumnIndex("type"));
                        comment.time = Long.parseLong(c.getString(c.getColumnIndex("time")));
                        comment.number = c.getString(c.getColumnIndex("number"));
                        comment.mac = c.getString(c.getColumnIndex("mac"));
                        comment.tid = c.getString(c.getColumnIndex("tid"));
                        comment.vipid = c.getString(c.getColumnIndex("member_id"));
                        comment.terminalType = c.getInt(c.getColumnIndex("terminal_type"));
                        data.add(comment);
                        i++;
                    }
                    c.close();
                }
                listener.done(data);
            }
        });
    }

    public synchronized void queryCacheAmount(final OnDoneListener<Integer> listener) {
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<ModelComment> data = new ArrayList<>();
                Cursor c = db.query(TABLE_CACHES, null, null, null, null, null, null);
                if (c != null) {
                    listener.done(c.getCount());
                } else {
                    listener.done(0);
                }
            }
        });
    }

    public void queryWatchs(final OnDoneListener<JSONArray> listener) {
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Cursor c = db.query(TABLE_SEND, null, null, null, null, null, null);
                JSONArray array = new JSONArray();
                if (c != null && c.getCount() > 0) {
                    while (c.moveToNext()) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("type", c.getInt(c.getColumnIndex("type")));
                            json.put("time", c.getString(c.getColumnIndex("time")));
                            json.put("number", c.getString(c.getColumnIndex("number")));
                            array.put(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    c.close();
                }
                listener.done(array);
            }
        });
    }

    public synchronized void newYear() {
        Log.d(TAG, "newYear: ");
        updateSetting("year", Calendar.getInstance().get(Calendar.YEAR) + "");
        ContentValues cv = new ContentValues();
        cv.put("yc", 0);
        cv.put("ygc", 0);
        db.update(TABLE_DATA, cv, "id = ?", new String[]{"1"});
        updateLastId(0);
        notifyUpdate();
    }

    public synchronized void newDay() {
        Log.d(TAG, "newDay: ");
        updateSetting("date", new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()));
        ContentValues cv = new ContentValues();
        cv.put("dc", 0);
        cv.put("dgc", 0);
        db.update(TABLE_DATA, cv, "id = ?", new String[]{"1"});
        notifyUpdate();
    }

    public synchronized void insertComment(final ModelComment modelComment, final OnDoneListener<ModelComment> listener) {
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "insertComment");
                updateYearsCount(getYearsCount() + 1);
                updateDaysCount(getDaysCount() + 1);
                if (modelComment.type == 1) {
                    updateYearsGoodCount(getYearsGoodCount() + 1);
                    updateDaysGoodCount(getDaysGoodCount() + 1);
                }
                ContentValues cv = new ContentValues();
                cv.put("number", modelComment.number);
                cv.put("time", modelComment.time + "");
                cv.put("type", modelComment.type);
                cv.put("mac", modelComment.mac);
                cv.put("tid", modelComment.tid);
                cv.put("member_id", modelComment.vipid);
                cv.put("terminal_type", modelComment.terminalType);
                db.insert(TABLE_CACHES, null, cv);
                if (modelComment.badComment()) {
                    cv.clear();
                    cv.put("number", modelComment.number);
                    cv.put("time", modelComment.time + "");
                    cv.put("type", modelComment.type);
                    db.insert(TABLE_SEND, null, cv);
                    Cursor c = db.query(TABLE_SEND, null, null, null, null, null, null);
                    if (c != null) {
                        int size = c.getCount();
                        c.close();
                        if (size > 20) {//最多保留20条watch数据
                            db.execSQL("delete from " + TABLE_SEND + " where id in (select id from " + TABLE_SEND + " order by id limit 0," + (size - 20) + ") ");
                        }
                    }
                }
                notifyUpdate();
                if (listener != null)
                    listener.done(modelComment);
            }
        });
    }

    public synchronized void sync(final StoreAmout storeAmout) {
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "syncData: " + storeAmout.toString());
                StoreAmout local = getCacheCount();
                updateLastId(storeAmout.year_count);
                updateYearsCount(storeAmout.year_count + local.year_count);
                updateYearsGoodCount(storeAmout.year_count_score + local.year_count_score);
                updateDaysCount(storeAmout.to_days_count + local.to_days_count);
                updateDaysGoodCount(storeAmout.to_days_count_score + local.to_days_count_score);
                notifyUpdate();
            }
        });
    }

    public synchronized void uploded(int size) {
        updateLastId(getLastId() + size);
        clearCaches(size);
    }

    public synchronized void updateLastId(int lastid) {
        ContentValues cv = new ContentValues();
        cv.put("lastid", lastid);
        db.update(TABLE_DATA, cv, "id = ?", new String[]{"1"});
    }

    private synchronized void updateSetting(String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(key, value);
        db.update(TABLE_SETTING, cv, "id = ?", new String[]{"1"});
    }

    public synchronized void updateStore(String store) {
        updateSetting("store", store);
    }

    public synchronized void updateIp(String ip) {
        updateSetting("ip", ip);
    }

    public synchronized void updateHttp(String http) {
        updateSetting("http", http);
    }

    public synchronized void updateYearsGoodCount(int count) {
        ContentValues cv = new ContentValues();
        cv.put("ygc", count);
        db.update(TABLE_DATA, cv, "id = ?", new String[]{"1"});
    }

    public synchronized void updateDaysGoodCount(int count) {
        ContentValues cv = new ContentValues();
        cv.put("dgc", count);
        db.update(TABLE_DATA, cv, "id = ?", new String[]{"1"});
    }

    public synchronized void updateYearsCount(int count) {
        ContentValues cv = new ContentValues();
        cv.put("yc", count);
        db.update(TABLE_DATA, cv, "id = ?", new String[]{"1"});
    }

    public synchronized void updateDaysCount(int count) {
        ContentValues cv = new ContentValues();
        cv.put("dc", count);
        db.update(TABLE_DATA, cv, "id = ?", new String[]{"1"});
    }

    public synchronized void clearCaches(final int n) {
        if (n > 0)
            singleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    db.execSQL("delete from " + TABLE_CACHES + " where id in (select id from " + TABLE_CACHES + " order by id limit 0," + n + ")");
                }
            });
    }

    public synchronized void clearWatchs(final int size) {
        if (size > 0)
            singleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    db.execSQL("delete from " + TABLE_SEND + " where id in (select id from " + TABLE_SEND + " order by id limit 0," + size + ")");
                }
            });
    }

    public void closeDb() {
        if (dbManagerList != null)
            dbManagerList.clear();
        if (!singleThreadExecutor.isShutdown()) {
            singleThreadExecutor.shutdown();
        }
        if (db != null && db.isOpen()) {
            db.close();
        }
        singleThreadExecutor = null;
    }

    public void clearData() {
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ContentValues cv = new ContentValues();
                cv.put("dc", 0);
                cv.put("dgc", 0);
                cv.put("yc", 0);
                cv.put("ygc", 0);
                cv.put("lastid", 0);
                db.update(TABLE_DATA, cv, "id = ?", new String[]{"1"});
                db.delete(TABLE_SEND, null, null);
                db.delete(TABLE_CACHES, null, null);
            }
        });
    }

    private void notifyUpdate() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (OnDbListener onDbListener : dbManagerList) {
                    onDbListener.notifyUpdate(DBManager.this);
                }
            }
        });
    }

    public void addListener(OnDbListener listener) {
        if (listener != null)
            dbManagerList.add(listener);
    }

    public void removeListener(OnDbListener listener) {
        if (listener != null)
            dbManagerList.remove(listener);
    }

    public interface OnDoneListener<T> {
        void done(T t);
    }

    public interface OnDbListener {
        void notifyUpdate(DBManager db);
    }
}