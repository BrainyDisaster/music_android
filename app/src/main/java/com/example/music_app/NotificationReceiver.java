package com.example.music_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.example.music_app.ApplicationClass.ACTION_NEXT;
import static com.example.music_app.ApplicationClass.ACTION_PLAY;
import static com.example.music_app.ApplicationClass.ACTION_PREVIOUS;
import static com.example.music_app.ApplicationClass.ACTION_STOP;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action_name = intent.getAction();
        Intent service_intent = new Intent(context,MusicService.class);
        if(action_name != null){
            switch (action_name){
                case ACTION_PLAY:
                    service_intent.putExtra("ActionName","playPause");
                    context.startService(service_intent);
                    break;
                case ACTION_NEXT:
                    service_intent.putExtra("ActionName","next");
                    context.startService(service_intent);
                    break;
                case ACTION_PREVIOUS:
                    service_intent.putExtra("ActionName","previous");
                    context.startService(service_intent);
                    break;
                case ACTION_STOP:
                    service_intent.putExtra("ActionName","stop");
                    context.startService(service_intent);
                    break;
            }
        }
    }
}
