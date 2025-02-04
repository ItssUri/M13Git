package com.example.musicplayer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int ACTIVITAT_SELECCIONAR_IMATGE = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        // Sección de la música

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    REQUEST_CODE);
//        }
//        @Override
//        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//            if (requestCode == REQUEST_CODE) {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission granted
//                    listDownloads();
//                } else {
//                    Log.d("Permission", "Permission denied to read Downloads directory.");
//                }
//            }
//        }
        // Sección del video
        // Controles del video
        MediaController mediaController = new MediaController(this);
        // Sección lista de videos/música dentro de raw
        File directorio = new File(Environment.getExternalStorageDirectory().getPath() +"/Download/");
        File[] files = directorio.listFiles();
        ArrayList<Song> ovmSongList = new ArrayList<Song>();
        Integer integer = 0;
        for (File file : files) {
            Log.d("INFO", "onCreate: " + file.getName());
            if (file.isFile()) {
                Log.d("INFO", "onCreate: " + file.getName());
                ovmSongList.add(new Song(
                        integer,
                        file.getName(),
                        (int) file.length()
                ));
            }
        }
        Adaptador adap = new Adaptador(ovmSongList, this);
        ListView lista = (ListView) findViewById(R.id.ovmSongListView);
        lista.setAdapter(adap);
    }
}