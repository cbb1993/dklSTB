package com.huanhong.decathlonstb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.huanhong.decathlonstb.util.ChannelMannager;

public class ErrorActivity extends Activity {

    public static void start(Context context) {
        Intent starter = new Intent(context, ErrorActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        ((TextView)findViewById(R.id.error_history_version)).append(ChannelMannager.getLocalChannel());
        ((TextView)findViewById(R.id.error_current_version)).append(ChannelMannager.getChannel());
    }
}