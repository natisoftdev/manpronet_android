package com.autostart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.servizi.BackgroundTicketService;

public class autostart extends BroadcastReceiver {

    public void onReceive(Context arg0, Intent arg1) {
        Intent intent = new Intent(arg0, BackgroundTicketService.class);
        //Intent intent2 = new Intent(arg0, NotificationService.class);
        arg0.startService(intent);
        //arg0.startService(intent2);
        Log.i("Autostart", "started");
    }
}
