package com.example.music_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.example.music_app.MainActivity.musicFiles;


public class SongsFragment extends Fragment {


    RecyclerView recyclerView;
    static MusicAdapter musicAdapter; // Static for update MusicApdater in SongsFragment
    public SongsFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.recycview);
        recyclerView.setHasFixedSize(true);
        // Update musicFiles from MusicAdapter because of search button
        if(!(musicFiles.size() <1)){
            musicAdapter = new MusicAdapter(getContext(),musicFiles);
            recyclerView.setAdapter(musicAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        }
        return view;
    }
}