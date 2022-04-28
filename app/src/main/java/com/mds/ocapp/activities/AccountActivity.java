package com.mds.ocapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mds.ocapp.R;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;

public class AccountActivity extends AppCompatActivity {

    TextView txtNamePerson, txtLastSync;
    LinearLayout layoutCreateBackup, layoutRetoreBackup, layoutLogout;

    BaseApp baseApp = new BaseApp(this);
    FunctionsApp functionsapp = new FunctionsApp(this);
    SPClass spClass = new SPClass(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Cuenta");

        layoutCreateBackup = findViewById(R.id.layoutCreateBackup);
        layoutRetoreBackup = findViewById(R.id.layoutRetoreBackup);
        layoutLogout = findViewById(R.id.layoutLogout);

        txtNamePerson = findViewById(R.id.txtNamePerson);
        txtLastSync = findViewById(R.id.txtLastSync);

        layoutCreateBackup.setOnClickListener(v -> baseApp.backupDBRealm());
        layoutRetoreBackup.setOnClickListener(v -> functionsapp.goRestoreDBActivity());
        layoutLogout.setOnClickListener(v -> baseApp.logout());

        if(!spClass.strGetSP("name").equals("ND") && !spClass.strGetSP("name").equals("")){
            txtNamePerson.setText("Hola, " + spClass.strGetSP("name"));
        }else{
            txtNamePerson.setText("Hola");
        }

        updateLastDateSync();
    }

    public void updateLastDateSync(){
        SPClass spClass = new SPClass(this);

        try {
            String date_last_sync = (spClass.strGetSP("date_last_sync")) == "ND" ? "nunca" : spClass.strGetSP("date_last_sync");
            if (date_last_sync.equals("nunca")) {
                baseApp.setTextView(findViewById(R.id.txtLastSync), "Última descarga de datos: nunca");
            } else {
                baseApp.setTextView(findViewById(R.id.txtLastSync), "Última descarga de datos: " + baseApp.dateFormat(baseApp.convertDate(date_last_sync)));
            }

            if (spClass.intGetSP("errors_last_sync") > 0) {
                baseApp.setTextView(findViewById(R.id.txtLastSync), txtLastSync.getText().toString() + " (con error)");
                txtLastSync.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            } else {
                txtLastSync.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }
        }catch (Exception e){
            Log.e("Error", "" + e);
        }
    }

    public void backFunction(){
        finish();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == android.R.id.home) {
            backFunction();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed(){
        backFunction();
    }
}
