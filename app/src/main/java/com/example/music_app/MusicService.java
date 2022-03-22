package com.example.music_app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import static com.example.music_app.ApplicationClass.ACTION_NEXT;
import static com.example.music_app.ApplicationClass.ACTION_PLAY;
import static com.example.music_app.ApplicationClass.ACTION_PREVIOUS;
import static com.example.music_app.ApplicationClass.ACTION_STOP;
import static com.example.music_app.ApplicationClass.CHANNEL_ID_2;
import static com.example.music_app.PlayerActivity.listSong;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder myBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIS_NAME = "ARTIS_NAME";
    public static final String SONG_NAME = "SONG_NAME";
    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind","Method");
        return myBinder;
    }



    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition",-1);
        String action_name = intent.getStringExtra("ActionName");
        if(myPosition != -1){
            playMedia(myPosition);
        }
        if(action_name != null){
            switch (action_name){
                case "playPause":
                    Toast.makeText(MusicService.this,"play_Pause",Toast.LENGTH_SHORT).show();
                    button_play_pause();
                    break;
                case "next":
                    Toast.makeText(MusicService.this,"next",Toast.LENGTH_SHORT).show();
                    button_next();
                    break;
                case "previous":
                    Toast.makeText(MusicService.this,"previous",Toast.LENGTH_SHORT).show();
                    button_previous();
                    break;
                case "stop":
                    if(mediaPlayer.isPlaying()){
                        stop();
                        stopForeground(true);
                        Log.e("Music","close");
                    }
                    else {

                        stopSelf();
                        Log.e("Music","closem");
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles = listSong ;
        position = startPosition;
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles !=null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start(){
        mediaPlayer.start();
    }

    boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    void stop(){
        mediaPlayer.stop();
    }

    void release(){
        mediaPlayer.release();
    }

    int getDuration(){
       return mediaPlayer.getDuration();
    }

    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }

    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    void createMediaPlayer(int positionInner){
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_FILE_LAST_PLAYED,MODE_PRIVATE)
                .edit();
        editor.putString(MUSIC_FILE,uri.toString());
        editor.putString(ARTIS_NAME, musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.apply();
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    void pause(){
        mediaPlayer.pause();
    }


    void onCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    // Change to next song if the song finish
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying != null) {
            actionPlaying.btn_next();
            if(mediaPlayer != null){
                createMediaPlayer(position);
                mediaPlayer.start();
                onCompleted();
            }
        }

    }

    // Set ActionPlaying for PlayerActivity recognize button in notification
    void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }

    // Set play, next, previous button on notification
    void showNotification(int playPause){
        Intent intent = new Intent(this,PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        Intent pre_intent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent pre_pendingIntent = PendingIntent.getBroadcast(this,
                0, pre_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pause_intent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pause_pendingIntent = PendingIntent.getBroadcast(this,
                0, pause_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent next_intent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent next_pendingIntent = PendingIntent.getBroadcast(this,
                0, next_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stop_intent = new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_STOP);
        PendingIntent stop_pendingIntent = PendingIntent.getBroadcast(this,
                0, stop_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set Background for notification
        byte[] picture = null;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if(picture != null){
            BitmapFactory.Options opts;
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        else {
            thumb = BitmapFactory.decodeResource(getResources(),R.drawable.bg);
        }
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPause)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_previous,"Previous",pre_pendingIntent)
                .addAction(playPause,"Pause",pause_pendingIntent)
                .addAction(R.drawable.ic_next,"Next",next_pendingIntent)
                .addAction(R.drawable.ic_clear,"Stop",stop_pendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        startForeground(2,notification); // When close the app music still playing (get permission on manifests)
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    void button_next(){
        if(actionPlaying != null){
            actionPlaying.btn_next();
            Log.e("Music","next");
        }
    }

    void button_play_pause(){
        if(actionPlaying != null){
            actionPlaying.btn_playpause();
            Log.e("Music","playPause");
        }
    }

    void button_previous(){
        if(actionPlaying != null){
            actionPlaying.btn_previous();
            Log.e("Music","previous");
        }
    }
}

