package com.example.musicplayer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LongDef;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Adaptador extends BaseAdapter {
    ArrayList<Song> ovmSongs;
    Context c;
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public Adaptador(ArrayList<Song> ovmSongs, Context c) {
        this.c=c;
        this.ovmSongs = ovmSongs;
    }
    @Override
    public int getCount() {
        return ovmSongs.size();
    }
    @Override
    public Object getItem(int i) {
        return ovmSongs.get(i);
    }
    @Override
    public long getItemId(int i) {
        return ovmSongs.get(i).getId();
    }
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater=(LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") final View vista_elemento = inflater.inflate(R.layout.song_list_element,viewGroup,false);

        // Pongo el texto
        TextView texto = (TextView) vista_elemento.findViewById(R.id.textoVista);
        texto.setText(ovmSongs.get(i).getName());
        if  (i % 2 == 0) {
            vista_elemento.setBackgroundColor(Color.parseColor("#121212"));
        }
        else {
            vista_elemento.setBackgroundColor(Color.parseColor("#171717"));
        }
        // Extraer metadato
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        Uri uri = Uri.parse(filePath+"/"+ ovmSongs.get(i).name);
        MediaMetadataRetriever m_metaRetriever = new MediaMetadataRetriever ();
        m_metaRetriever.setDataSource(c,uri);
        Intent ovmIntent = new Intent(c,player.class);
        String albumName = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String artist = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String trackName = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String duration = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String imageheight = m_metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER);
        Log.d("INFO", "getView: Album = " + albumName + " | " + artist + " | " + trackName );
        ImageView ovmAlbumArt = (ImageView) vista_elemento.findViewById(R.id.albumArt);
        TextView ovmTrackTextView = (TextView) vista_elemento.findViewById(R.id.textoVista);
        Button ovmPlaySong = (Button) vista_elemento.findViewById(R.id.play);
        try {
            byte[] artBytes =  m_metaRetriever.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
            ovmAlbumArt.setImageBitmap(bitmap);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream); // Compress to 50% quality
            byte[] byteArray = stream.toByteArray();
            ovmIntent.putExtra("albumArt", byteArray);
        } catch (Exception e) {
            ovmAlbumArt.setImageResource(R.drawable.placeholder_album_art);
        }
        if (trackName!=null) {
            String formattedDuration = formatMillisecondsToMinutesAndSeconds(Long.valueOf(duration));
            ovmTrackTextView.setText(trackName + " [" + formattedDuration + "]");
        }
        else {
            String formattedDuration = formatMillisecondsToMinutesAndSeconds(Long.valueOf(duration));
            ovmTrackTextView.append(" [" + formattedDuration + "]");
        }
        try {
            m_metaRetriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ovmPlaySong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (trackName==null) {
                    ovmIntent.putExtra("trackName",ovmSongs.get(i).getName());
                } else {
                    ovmIntent.putExtra("trackName",trackName);
                }
                if (artist == null) {
                    ovmIntent.putExtra("artist","Unknown Artist");
                } else {
                    ovmIntent.putExtra("artist",artist);
                }

                if (albumName == null) {
                    ovmIntent.putExtra("albumName","Unknown");
                } else {
                    ovmIntent.putExtra("albumName",albumName);
                }


                ovmIntent.putExtra("duration",duration);
                ovmIntent.putExtra("imageHeight",imageheight);
                ovmIntent.putExtra("test","test");
                ovmIntent.putExtra("fileUri", uri.toString());
                Bundle extras = ovmIntent.getExtras();
                if (extras != null) {
                    for (String key : extras.keySet()) {
                        Object value = extras.get(key);
                        Log.d("IntentDebug", "Extra [" + key + "]: " + value);
                        Log.d("INFO", "onClick: " + uri.toString());
                    }
                } else {
                    Log.d("IntentDebug", "No extras in Intent");
                }
                c.startActivity(ovmIntent);
            }
        });

        return vista_elemento;
    }

    private static String formatMillisecondsToMinutesAndSeconds(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60; // Get remaining seconds

        return String.format("%02d:%02d", minutes, seconds);
    }
}

