package org.minimarex.minimacore.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.minima.utils.MinimaLogger;

public class Alarm extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent){
        MinimaLogger.log("MINIMA ALARM RECEIVED : Start Service");

        //Create the Minima Service Intent
        try{
            Intent serviceintent = new Intent(context, MinimaService.class);
            context.startForegroundService(serviceintent);
        }catch(Exception exc){
            MinimaLogger.log("Cannot start foreground service : "+exc);
        }

        //Send a start service JOB
        //ServiceStarterJobService.enqueueWork(context, new Intent());
    }

    public void setAlarm(Context context){
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Alarm.class);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, flags);

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR , pi);
    }

    public void cancelAlarm(Context context){
        Intent intent = new Intent(context, Alarm.class);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, flags);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
    }
}
