package com.huanhong.decathlonstb.event;

import com.huanhong.decathlonstb.MainActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent sayHelloIntent = new Intent(context, MainActivity.class);
			sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(sayHelloIntent);
		}
	}
}
