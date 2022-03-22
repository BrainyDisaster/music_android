package com.example.music_app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {
    public static final String CHANNEL_ID_1 = "channel_1";
    public static final String CHANNEL_ID_2 = "channel_2";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_STOP = "action_stop";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel1();
    }

    private void createNotificationChannel1(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_ID_1, "channel_1", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Channel 1 ...");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_ID_2, "channel_2", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("Channel 2 ...");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel2);
        }
    }
}
