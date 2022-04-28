package com.mds.ocapp.activities;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.mds.ocapp.R;
import com.mds.ocapp.api.RetrofitClient;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.models.WResponse;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;

public class AboutActivity extends AppCompatActivity {

    int ALL_PERMISSIONS = 101, localVersionCode;
    boolean statusAlert = false;
    String localVersionName, serverVersion, urlApk, nameApk;
    TextView txtVersion, txtInfoVersion, txtVersionServer, txtInfoError;
    ImageView imgLogoKev;
    Button btnUpdate;

    BaseApp baseApp = new BaseApp(this);
    FunctionsApp functionsapp = new FunctionsApp(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        urlApk = "http://mds-sistemas.com/documentos/Android/DiCampoOC.apk";
        nameApk = "DiCampoOC.apk";

        txtVersion = findViewById(R.id.txtVersion);
        txtInfoVersion = findViewById(R.id.txtInfoVersion);
        txtVersionServer = findViewById(R.id.txtVersionServer);
        txtInfoError = findViewById(R.id.txtInfoError);
        imgLogoKev = findViewById(R.id.imgLogoKev);

        btnUpdate = findViewById(R.id.btnUpdate);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            localVersionName = pInfo.versionName;
            localVersionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(localVersionName != null){
            txtVersion.setText("Version " + localVersionName);
        }

        getLastVersion();

        btnUpdate.setOnClickListener(v -> {
            try {
                downloadFile(AboutActivity.this, urlApk, nameApk);
            }catch (Exception ex){
                baseApp.showAlert("Error", "Ocurrió un error, repórtalo al Dpto de Sistemas: " + ex);
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                verifyNewUpdate();
                handler.postDelayed(this, 1000);
            }
        }, 1000);  //the time is in miliseconds

        imgLogoKev.setOnClickListener(v-> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/kevsslsch.portfolio/"));
            startActivity(browserIntent);
        });

        refreshStatusButton();
    }

    public void getLastVersion(){

        Call<WResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .getLastVersion();

        call.enqueue(new Callback<WResponse>() {
            @Override
            public void onResponse(Call<WResponse> call, retrofit2.Response<WResponse> response) {

                if (response.body() != null) {
                    WResponse wResponse = response.body();
                    txtVersionServer.setText(wResponse.getLast_version());
                    verifyNewUpdate();
                }
            }
            @Override
            public void onFailure(Call<WResponse> call, Throwable t) {
                if(baseApp.isSuperUser()) {
                    baseApp.showAlert("Error SUPERUSER", "Ocurrió el error al cargar la última versión del Servidor: " + t.getMessage());
                }
            }
        });

    }

    public void refreshStatusButton(){
        //Permisos
        if(checkPermissions()){
            btnUpdate.setEnabled(true);
        }else{
            btnUpdate.setEnabled(false);
        }
    }

    public void verifyNewUpdate(){

        serverVersion =  txtVersionServer.getText().toString();
        txtInfoVersion.setVisibility(View.INVISIBLE);
        btnUpdate.setVisibility(View.INVISIBLE);
        txtInfoError.setVisibility(View.INVISIBLE);

        if(!serverVersion.equals("0.0.0")) {
            if(!serverVersion.equals(localVersionName)){
                txtInfoVersion.setVisibility(View.INVISIBLE);
                btnUpdate.setVisibility(View.VISIBLE);
            }else if(serverVersion.equals(localVersionName)){
                txtInfoVersion.setVisibility(View.VISIBLE);
                btnUpdate.setVisibility(View.INVISIBLE);
            }

            txtInfoError.setVisibility(View.INVISIBLE);
        }else{
            txtInfoError.setVisibility(View.VISIBLE);
            txtInfoVersion.setVisibility(View.INVISIBLE);
            btnUpdate.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Used to download the file from url.
     * <p/>
     * 1. Download the file using Download Manager.
     *
     * @param url      Url.
     * @param fileName Files Name.
     */
    public void downloadFile(final Activity activity, final String url, final String fileName) {
        try {
            if (url != null && !url.isEmpty()) {
                Uri uri = Uri.parse(url);
                activity.registerReceiver(attachmentDownloadCompleteReceive, new IntentFilter(
                        DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setMimeType(getMimeType(uri.toString()));
                request.setTitle(getResources().getString(R.string.app_name));
                request.setDescription("Descargando actualización...");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                dm.enqueue(request);

                //unregisterReceiver(attachmentDownloadCompleteReceive);
            }
        } catch (IllegalStateException e) {
            Toast.makeText(activity, "Please insert an SD card to download file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Used to get MimeType from url.
     *
     * @param url Url.
     * @return Mime Type for the given url.
     */
    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Attachment download complete receiver.
     * <p/>
     * 1. Receiver gets called once attachment download completed.
     * 2. Open the downloaded file.
     */
    BroadcastReceiver attachmentDownloadCompleteReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                openDownloadedAttachment(context, downloadId);
            }
        }
    };

    /**
     * Used to open the downloaded attachment.
     *
     * @param context    Content.
     * @param downloadId Id of the downloaded file to open.
     */
    private void openDownloadedAttachment(final Context context, final long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String downloadLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String downloadMimeType = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
            if ((downloadStatus == DownloadManager.STATUS_SUCCESSFUL) && downloadLocalUri != null) {
                openDownloadedAttachment(context, Uri.parse(downloadLocalUri), downloadMimeType);
            }
        }
        cursor.close();
    }

    /**
     * Used to open the downloaded attachment.
     * <p/>
     * 1. Fire intent to open download file using external application.
     *
     * 2. Note:
     * 2.a. We can't share fileUri directly to other application (because we will get FileUriExposedException from Android7.0).
     * 2.b. Hence we can only share content uri with other application.
     * 2.c. We must have declared FileProvider in manifest.
     * 2.c. Refer - https://developer.android.com/reference/android/support/v4/content/FileProvider.html
     *
     * @param context            Context.
     * @param attachmentUri      Uri of the downloaded attachment to be opened.
     * @param attachmentMimeType MimeType of the downloaded attachment.
     */
    private void openDownloadedAttachment(final Context context, Uri attachmentUri, final String attachmentMimeType) {
        if(attachmentUri!=null) {
            // Get Content Uri.
            if (ContentResolver.SCHEME_FILE.equals(attachmentUri.getScheme())) {
                // FileUri - Convert it to contentUri.
                File file = new File(attachmentUri.getPath());
                attachmentUri = FileProvider.getUriForFile(this, "com.freshdesk.helpdesk.provider", file);;
            }

            Intent openAttachmentIntent = new Intent(Intent.ACTION_VIEW);
            openAttachmentIntent.setDataAndType(attachmentUri, attachmentMimeType);
            openAttachmentIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.startActivity(openAttachmentIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "Ocurrió un error al abrir el archivo, repórtalo al dpto de Sistemas: " + e, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT< Build.VERSION_CODES.M){//validamos si estamos en android menor a 6 para no buscar los permisos
            return true;
        }

        //validamos si los permisos ya fueron aceptados
        // if((getApplicationContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)&&getApplicationContext().checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED){
        if((getApplicationContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)){
            return true;
        }

        //if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)||(shouldShowRequestPermissionRationale(CAMERA)))){
        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE))){
            showAlertNotPermissions();
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, ALL_PERMISSIONS);
        }

        return false;//implementamos el que procesa el evento dependiendo de lo que se defina aqui
    }

    public void showAlertNotPermissions(){

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Permisos desactivados");
            dialog.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

            dialog.setPositiveButton("Aceptar", (dialogInterface, i) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                }
            });

            dialog.show();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == android.R.id.home) {
            if(baseApp.returnSessionVerify()){
                functionsapp.goMainActivity();
            }else{
                functionsapp.goLoginActivity();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if(baseApp.returnSessionVerify()){
            functionsapp.goMainActivity();
        }else{
            functionsapp.goLoginActivity();
        }
    }
}
