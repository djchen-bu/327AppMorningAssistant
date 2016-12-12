package com.example.djchen.morningassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class Alarm_Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("We are in the receiver","yay");

        String get_your_string = intent.getExtras().getString("extra");
        //Create an intent to ringtone service
        Intent service_intent = new Intent(context, RingtonePlayingService.class);

        service_intent.putExtra("extra",get_your_string);

        context.startService(service_intent);
    }
}
