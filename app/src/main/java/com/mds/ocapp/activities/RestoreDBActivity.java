package com.mds.ocapp.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mds.ocapp.R;
import com.mds.ocapp.adapters.AdapterFiles;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.classes.SpacesItemDecoration;
import com.mds.ocapp.models.Files;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class RestoreDBActivity extends AppCompatActivity {

    BaseApp baseApp = new BaseApp(this);
    FunctionsApp functionsApp = new FunctionsApp(this);

    RecyclerView recyclerViewFiles;
    ArrayList<Files> filesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_dbbackup);
        setTitle("Recuperar una Base de Datos");

        recyclerViewFiles = findViewById(R.id.recyclerViewFiles);

        GridLayoutManager mGridLayoutManagerDetails = new GridLayoutManager(this, 1);
        recyclerViewFiles.setLayoutManager(mGridLayoutManagerDetails);
        recyclerViewFiles.setItemAnimator(new DefaultItemAnimator());
        recyclerViewFiles.addItemDecoration(new SpacesItemDecoration(1));

        getFilesRealm();
    }

    public void getFilesRealm() {
        Files file;
        String size;

        if (baseApp.statusPermissionWriteExternalStorage()) {

            if (filesList != null) {
                filesList.clear();
            }

            String path;

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                path = Environment.getExternalStorageDirectory().toString() + "/dicampooc/Databases";
            } else {
                path = getExternalFilesDir(null) + "/dicampooc/Databases";
            }

            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();

            if (files != null) {

                Log.d("Files", "Size: " + files.length);

                if (files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        Log.d("Files", "FileName:" + files[i].getName() + ", " + files[i].length());

                        if ((files[i].length() / 1000) >= 1024) {
                            size = (files[i].length() / 1000) / 1024 + " Mb";
                        } else {
                            size = (files[i].length() / 1000) + " Kb";
                        }

                        String dateString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(files[i].lastModified()));

                        file = new Files(
                                files[i].getName(),
                                size,
                                dateString);

                        filesList.add(file);
                    }
                } else {
                    baseApp.showToast("No hay Bases de Datos para recuperar");
                    finish();
                }

                AdapterFiles adapterFiles = new AdapterFiles(this, filesList);

                recyclerViewFiles.setItemAnimator(new DefaultItemAnimator());
                recyclerViewFiles.setAdapter(adapterFiles);
            }else{
                baseApp.showToast("No hay archivos disponibles para recuperar");
                finish();
            }

        } else {
            finish();
        }
    }

    public void onResume() {
        super.onResume();
        getFilesRealm();
    }
}
