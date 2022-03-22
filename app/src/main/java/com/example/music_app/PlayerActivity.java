package com.example.music_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.animation.AnimatorSet;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import static com.example.music_app.AlbumDetailsAdapter.albumFiles;
import static com.example.music_app.ApplicationClass.ACTION_NEXT;
import static com.example.music_app.ApplicationClass.ACTION_PLAY;
import static com.example.music_app.ApplicationClass.ACTION_PREVIOUS;
import static com.example.music_app.ApplicationClass.CHANNEL_ID_2;
import static com.example.music_app.MainActivity.check_repeat;
import static com.example.music_app.MainActivity.check_shuffle;
import static com.example.music_app.MainActivity.check_playPause;
import static com.example.music_app.MainActivity.musicFiles;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {
    TextView txt_songname, txt_artist, txt_duration_start, txt_duration_end;
    ImageView img_next, img_previous, img_shuffle, img_repeat, img_backgound;
    FloatingActionButton floatingActionButton;
    SeekBar seekBar;
    int position = -1;
    public static ArrayList<MusicFiles> listSong = new ArrayList<>();
    public static Uri uri;
//    public static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread thread_play, thread_previous, thread_next;
    MusicService musicService;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();

        initView();
        getIntenMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicService != null && fromUser){
                    musicService.seekTo(progress *1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Give seekbar update duration
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService != null){
                    int current_position = musicService.getCurrentPosition() /1000;
                    seekBar.setProgress(current_position);
                    txt_duration_start.setText(customTime(current_position));
                }
                handler.postDelayed(this,1000);
            }
        });

        img_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check_shuffle){
                     check_shuffle =false;
                     img_shuffle.setImageResource(R.drawable.ic_shuffle);
                }
                else {
                    check_shuffle = true;
                    img_shuffle.setImageResource(R.drawable.ic_shuffle_off);
                }
            }
        });

        img_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check_repeat){
                    check_repeat = false;
                    img_repeat.setImageResource(R.drawable.ic_repeat);
                }
                else {
                    check_repeat = true;
                    img_repeat.setImageResource(R.drawable.ic_repeat_off);
                }
            }
        });

    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        Intent intent =new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        getIntenMethod();
        btn_thread_play();
        btn_thread_next();
        btn_thread_previous();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void btn_thread_play() {
        thread_play = new Thread(){
            @Override
            public void run() {
                super.run();
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btn_playpause();
                    }
                });
            }
        };

        thread_play.start();
        try {
            thread_play.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void btn_playpause() {
        if(musicService.isPlaying()) {
            check_playPause = false;
            floatingActionButton.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        if(musicService != null){
                            int current_position = musicService.getCurrentPosition() /1000;
                            seekBar.setProgress(current_position);
                        }
                        handler.postDelayed(this,1000);



                }
            });
        }
        else {
            check_playPause = true;
            musicService.showNotification(R.drawable.ic__pause);
            floatingActionButton.setImageResource(R.drawable.ic__pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int current_position = musicService.getCurrentPosition() /1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }
    // click on next
    private void btn_thread_next() {
        thread_next = new Thread(){
            @Override
            public void run() {
                super.run();
                img_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btn_next();
                    }
                });
            }
        };
        thread_next.start();
        try {
            thread_play.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void btn_next() {
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            // When button shuffle is clicked (true) play random song
            if(check_shuffle && !check_repeat){
                position = Random_song(listSong.size()-1);
            }
            //
            else if(!check_shuffle && !check_repeat){
                position = ((position+1) % listSong.size());
            }

            uri = Uri.parse(listSong.get(position).getPath());
//            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            musicService.createMediaPlayer(position);
            metaData(uri); //Set time music and set background
            txt_songname.setText(listSong.get(position).getTitle());
            txt_artist.setText(listSong.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int current_position = musicService.getCurrentPosition() /1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.onCompleted();
            check_playPause = true;
            musicService.showNotification(R.drawable.ic__pause);
            floatingActionButton.setBackgroundResource(R.drawable.ic__pause);
            musicService.start();
        }
        else {
            musicService.stop();
            musicService.release();
            // When button shuffle is clicked play random song
            if(check_shuffle && !check_repeat){
                position = Random_song(listSong.size() - 1);
            }
            else if(!check_shuffle && !check_repeat){
                position = ((position+1) % listSong.size());
            }

            uri = Uri.parse(listSong.get(position).getPath());
//            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            musicService.createMediaPlayer(position);
            metaData(uri);
            txt_songname.setText(listSong.get(position).getTitle());
            txt_artist.setText(listSong.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int current_position = musicService.getCurrentPosition() /1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.onCompleted();
            check_playPause = false;
            musicService.showNotification(R.drawable.ic_play);
            floatingActionButton.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private int Random_song(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }
    //------------------------------------------------------------------------

    // Click on previous
    private void btn_thread_previous() {
        thread_previous = new Thread(){
            @Override
            public void run() {

                super.run();
                img_previous.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btn_previous();
                    }
                });
            }
        };

        thread_previous.start();

        try {
            thread_play.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void btn_previous() {
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();

            if(check_shuffle && !check_repeat){
                position = Random_song(listSong.size() - 1);
            }
            else if(!check_shuffle && !check_repeat){
                position = ((position) - 1) < 0 ? (listSong.size() - 1 ): (position -1);
            }


            uri = Uri.parse(listSong.get(position).getPath());
//            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            musicService.createMediaPlayer(position);
            metaData(uri);
            txt_songname.setText(listSong.get(position).getTitle());
            txt_artist.setText(listSong.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int current_position = musicService.getCurrentPosition() /1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.onCompleted();
            check_playPause = true;
            musicService.showNotification(R.drawable.ic__pause);
            floatingActionButton.setBackgroundResource(R.drawable.ic__pause);
            musicService.start();
        }
        else {
            musicService.stop();
            musicService.release();

            if(check_shuffle && !check_repeat){
                position = Random_song(listSong.size() - 1);
            }
            else if(!check_shuffle && !check_repeat){
                position = ((position) - 1) < 0 ? (listSong.size() - 1 ): (position -1);
            }

            uri = Uri.parse(listSong.get(position).getPath());
//            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            musicService.createMediaPlayer(position);
            metaData(uri);
            txt_songname.setText(listSong.get(position).getTitle());
            txt_artist.setText(listSong.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int current_position = musicService.getCurrentPosition() /1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.onCompleted();
            check_playPause = false;
            musicService.showNotification(R.drawable.ic_play);
            floatingActionButton.setBackgroundResource(R.drawable.ic_play);
        }
        
    }
    //----------------------------------------------------------------


    //Custom time
    private String customTime(int current_position){
        String time = "";
        String time_new = "";
        String second =String.valueOf(current_position %60);
        String minute = String.valueOf(current_position /60);
        time = minute+":"+second;
        time_new = minute+":"+"0"+second;
        if(second.length() == 1){
            return  time_new;
        }
        else {
            return time;
        }
    }

    // Confirm the song when you click on listview or the album
    private void getIntenMethod() {
        position = getIntent().getIntExtra("position",-1);
        // Check when you click the song from album
        String sender = getIntent().getStringExtra("sender");

        // If sender is true change listSong to albumFiles, else is change to musicFiles which you click on the song not album
        if(sender !=null && sender.equals("albumDetails")){
            listSong = albumFiles;
        }
        // when you click on search button it update from MusicAdapter to choosen the song you search
        else {
            listSong = MusicAdapter.musicFiles;
        }

        if(listSong != null){
            check_playPause = true;
            floatingActionButton.setImageResource(R.drawable.ic__pause);
            uri = Uri.parse(listSong.get(position).getPath());
        }
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);

    }



    private void initView(){
        txt_songname = findViewById(R.id.txt_song_name);
        txt_artist = findViewById(R.id.txt_artist_name);
        txt_duration_start = findViewById(R.id.txt_duration_start);
        txt_duration_end = findViewById(R.id.txt_duration_end);
        img_next = findViewById(R.id.img_next);
        img_previous = findViewById(R.id.img_previous);
        img_shuffle = findViewById(R.id.img_shuffle);
        img_repeat = findViewById(R.id. img_repeat);
        img_backgound = findViewById(R.id.img_background_music);
        floatingActionButton = findViewById(R.id.fb_play);
        seekBar = findViewById(R.id.seekbar);
    }


    //Update backgound music according to the song  and set time music end
    private void metaData(Uri uri){
        MediaMetadataRetriever retriever =  new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal =Integer.parseInt(listSong.get(position).getDuration())/1000;
        txt_duration_end.setText(customTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();// find the picture of the song
        Bitmap bitmap ;

        if(art != null){
//            Glide.with(this)
//                    .asBitmap()
//                    .load(art)
//                    .into(img_backgound);


            bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            Image_animation(this,img_backgound,bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable @org.jetbrains.annotations.Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if(swatch != null){
                        ImageView img_gradiant =findViewById(R.id.img_background_gradiant);
                        RelativeLayout layout_player = findViewById(R.id.R_player);
                        img_gradiant.setBackgroundResource(R.drawable.bc_gradiant);
                        layout_player.setBackgroundResource(R.color.purple_200);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{swatch.getRgb(),0x00000000});
                        img_gradiant.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawable_bg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{swatch.getRgb(),swatch.getRgb()});
                        layout_player.setBackground(gradientDrawable_bg);
                        txt_songname.setTextColor(swatch.getTitleTextColor());
                        txt_artist.setTextColor(swatch.getTitleTextColor());
                    }
                    else {
                        ImageView img_gradiant =findViewById(R.id.img_background_gradiant);
                        RelativeLayout layout_player = findViewById(R.id.R_player);
                        img_gradiant.setBackgroundResource(R.drawable.bc_gradiant);
                        layout_player.setBackgroundResource(R.color.purple_200);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{0xff000000,0x00000000});
                        img_gradiant.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawable_bg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{0xff000000,0xff000000});
                        layout_player.setBackground(gradientDrawable_bg);
                        txt_songname.setTextColor(Color.WHITE);
                        txt_artist.setTextColor(Color.DKGRAY);
                    }
                }

            });
        }
        else {
//            Glide.with(this)
//                    .asBitmap()
//                    .load(R.drawable.bg)
//                    .into(img_backgound);
            Image_animation_default(this,img_backgound,R.drawable.bg);
            ImageView img_gradiant =findViewById(R.id.img_background_gradiant);
            RelativeLayout layout_player = findViewById(R.id.R_player);
            img_gradiant.setBackgroundResource(R.drawable.bc_gradiant);
            layout_player.setBackgroundResource(R.color.purple_200);
            txt_songname.setTextColor(Color.WHITE);
            txt_artist.setTextColor(Color.DKGRAY);
        }
    }
    // forced to have final at first to determined the picture of the song or it use default picture
    public void Image_animation(final Context context,final ImageView imageView,final Bitmap bitmap){
        Animation anima_out = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation anima_in = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        anima_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                anima_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(anima_in);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(anima_out);
    }

    // Show animation when click on the which have a default picture
    public void Image_animation_default(final Context context,final ImageView imageView,final int bitmap){
        Animation anima_out = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation anima_in = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        anima_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                anima_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(anima_in);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(anima_out);
    }



    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this); // setCallBack from MusicService
        Toast.makeText(PlayerActivity.this,"Connected" + musicService,Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration()/1000);
        metaData(uri);
        txt_songname.setText(listSong.get(position).getTitle());
        txt_artist.setText(listSong.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic__pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

}