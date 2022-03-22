package com.example.music_app;

import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {
    private Context context;
    static ArrayList<MusicFiles> musicFiles;

    public MusicAdapter(Context context,ArrayList<MusicFiles> musicFiles){
        this.context = context;
        this.musicFiles = musicFiles;
    }
    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_items,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MusicAdapter.MyViewHolder holder, int position) {
        holder.txt_name.setText(musicFiles.get(position).getTitle());
        byte[] image = getAlbumArt(musicFiles.get(position).getPath());
        if(image != null){
            Glide.with(context).asBitmap()
                    .load(image)
                    .into(holder.img_album);
        }
        else {
            Glide.with(context).asBitmap()
                    .load(R.drawable.bg)
                    .into(holder.img_album);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        });
        holder.img_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context,v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_popup,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.it_delete:
                                Toast.makeText(context,"Delete",Toast.LENGTH_SHORT).show();
                                deleteFiles(position,v);
                                break;
                        }
                        return true;
                    }
                });

            }
        });
    }
    //Delete Song
    private void deleteFiles(int position, View v) {
        // Get id of the song from phone storage
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                ,Long.parseLong(musicFiles.get(position).getId()));

        File file = new File(musicFiles.get(position).getPath());
        boolean delete = file.delete();
        if(delete){
            // Delete the song from phone storage
            context.getContentResolver().delete(uri,null,null);
            // Delete the song from app
            musicFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,musicFiles.size());
            Snackbar.make(v,"File delete ",Snackbar.LENGTH_SHORT).show();
        }
        else {
            Snackbar.make(v,"Can't delete File ",Snackbar.LENGTH_SHORT).show();
        }

    }

    @Override
    public int getItemCount() {
        return musicFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txt_name;
        ImageView img_album, img_more;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_name);
            img_album = itemView.findViewById(R.id.img_music);
            img_more = itemView.findViewById(R.id.img_more);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    // For search button in MainActivity
    void updateList(ArrayList<MusicFiles> musicFilesArrayList){
        musicFiles = new ArrayList<>();
        musicFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}
