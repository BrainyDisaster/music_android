package com.example.music_app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static android.content.Context.MODE_PRIVATE;
import static com.example.music_app.MainActivity.ARTIS_TO_FRAG;
import static com.example.music_app.MainActivity.PATH_TO_FRAG;
import static com.example.music_app.MainActivity.SHOW_MINI_PLAYER;
import static com.example.music_app.MainActivity.SONG_NAME_TO_FRAG;
import static com.example.music_app.MainActivity.albums;


public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageView img_next,img_picture_song;
    TextView txt_song_name, txt_artist;
    FloatingActionButton fb_playPause;
    View view;
    MusicService musicService;
    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIS_NAME = "ARTIS_NAME";
    public static final String SONG_NAME = "SONG_NAME";
    public static boolean CHECK = false;
    public NowPlayingFragmentBottom() {
        // Required empty public constructor

    }






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);
        txt_artist = view.findViewById(R.id.txt_artist_name_playing);
        txt_song_name = view.findViewById(R.id.txt_song_name_playing);
        img_next = view.findViewById(R.id.img_next_playing);
        img_picture_song = view.findViewById(R.id.img_picture_playing);
        fb_playPause = view.findViewById(R.id.fb_playPause_playing);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),PlayerActivity.class);
                startActivity(intent);
            }
        });


        img_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicService != null){

                    musicService.button_next();
                    if(getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE)
                                .edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                        editor.putString(ARTIS_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.musicFiles.get(musicService.position).getTitle());
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED,MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE,null);
                        String artist_name = preferences.getString(ARTIS_NAME,null);
                        String song_name = preferences.getString(SONG_NAME,null);
                        if(path != null){
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIS_TO_FRAG = artist_name;
                            SONG_NAME_TO_FRAG = song_name;
                            fb_playPause.setImageResource(R.drawable.ic__pause);


                        }
                        else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIS_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                            fb_playPause.setImageResource(R.drawable.ic_play);

                        }
                        if(SHOW_MINI_PLAYER){

                            if(PATH_TO_FRAG != null) {
                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if(art != null) {
                                    Glide.with(getContext()).load(art).into(img_picture_song);
                                }
                                else {
                                    Glide.with(getContext()).load(R.drawable.bg).into(img_picture_song);
                                }
                                txt_song_name.setText(SONG_NAME_TO_FRAG);
                                txt_artist.setText(ARTIS_TO_FRAG);
                                fb_playPause.setImageResource(R.drawable.ic__pause);
                            }
                        }


                    }
                }
            }
        });
        fb_playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.button_play_pause();
                if(musicService.isPlaying()){
                    fb_playPause.setImageResource(R.drawable.ic__pause);
                }
                else {
                    fb_playPause.setImageResource(R.drawable.ic_play);
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SHOW_MINI_PLAYER){
            if(PATH_TO_FRAG != null) {

                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if(art != null) {
                    Glide.with(getContext()).load(art).into(img_picture_song);
                }
                else {
                    Glide.with(getContext()).load(R.drawable.bg).into(img_picture_song);
                }
                    txt_song_name.setText(SONG_NAME_TO_FRAG);
                    txt_artist.setText(ARTIS_TO_FRAG);
                Intent intent = new Intent(getContext(),MusicService.class);
                if(getContext() != null){
                    getContext().bindService(intent,this, Context.BIND_AUTO_CREATE);
                }

            }

        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(getContext() != null){
//            getContext().unbindService(this);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }
}