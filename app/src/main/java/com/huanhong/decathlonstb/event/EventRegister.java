package com.huanhong.decathlonstb.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by done on 2018/3/9.
 */

public class EventRegister extends BroadcastReceiver {

    private Context rContext;
    private static EventRegister eventRegister;

    private List<IEvent> eventList = new ArrayList<>();

    public EventRegister(Context context) {
        rContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        for (IEvent iEvent : eventList) {
            if (iEvent.isOk()) {
                iEvent.ok();
            }
        }
    }

    public static void register(@NonNull Context context) {
        if (eventRegister == null) {
            eventRegister = new EventRegister(context);
        } else {
            unregister();
        }
        context.registerReceiver(eventRegister, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    public static void unregister() {
        if (eventRegister != null) {
            for (IEvent iEvent : eventRegister.eventList) {
                iEvent.end();
            }
            eventRegister.eventList.clear();
            eventRegister.rContext.unregisterReceiver(eventRegister);
            eventRegister = null;
        }
    }

    public static void addEvent(@NonNull IEvent event) {
        if (eventRegister == null)
            throw new NullPointerException("Not Register before add!");
        eventRegister.eventList.add(event);
    }
}