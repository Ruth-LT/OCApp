package com.mds.ocapp.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mds.ocapp.R;
import com.mds.ocapp.api.RetrofitClient;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.ConnectionClass;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;
import com.mds.ocapp.models.WResponse;

import retrofit2.Call;
import retrofit2.Callback;

public class ChangeConnectionActivity extends AppCompatActivity {

    Boolean localIPActived = false, defaulLocalIPActived = false, failConnection = false, unBlockedSystemIP = false;
    ProgressDialog progressDialog;

    SPClass spClass = new SPClass(this);
    BaseApp baseApp = new BaseApp(this);
    FunctionsApp functionsapp = new FunctionsApp(this);

    EditText editTxtConnectionIPLocal, editTxtConnectionIPExterna, editTxtConnectionDataBase, editTxtConnectionUser, editTxtConnectionPassword, editTxtPIN;
    Button btnSelectIPLocal, btnSelectIPExterna, btnCheckConnection, btnChangeConnection, btnOk;
    TextView txtTitleConnection, txtTitleIPLocal, txtIPCurrent;
    LinearLayout layoutButtonsSelect;
    Switch switchIPLocal;

    BottomSheetDialog menuBottomSheet;

    String strIP, PINUnblockSystemIP = null, pinSavedSP;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_connection);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        // Cambiar aqu?? el default para mostrar o no la IP Local
        defaulLocalIPActived = false;

        layoutButtonsSelect = findViewById(R.id.layoutButtonsSelect);

        btnSelectIPLocal = findViewById(R.id.btnSelectIPLocal);
        btnSelectIPExterna = findViewById(R.id.btnSelectIPExterna);
        btnCheckConnection = findViewById(R.id.btnCheckConnection);
        btnChangeConnection = findViewById(R.id.btnChangeConnection);

        editTxtConnectionIPLocal = findViewById(R.id.editTxtConnectionIPLocal);
        editTxtConnectionIPExterna = findViewById(R.id.editTxtConnectionIPExterna);
        editTxtConnectionDataBase = findViewById(R.id.editTxtConnectionDataBase);
        editTxtConnectionUser = findViewById(R.id.editTxtUser);
        editTxtConnectionPassword = findViewById(R.id.editTxtPassword);
        txtIPCurrent = findViewById(R.id.txtIPCurrent);
        txtTitleConnection = findViewById(R.id.txtTitleConnection);
        txtTitleIPLocal = findViewById(R.id.txtTitleIPLocal);

        switchIPLocal = findViewById(R.id.switchIPLocal);

        strIP = spClass.strGetSP("IPConnection");
        unBlockedSystemIP = spClass.boolGetSP("unBlockedSystemIP");
        localIPActived = spClass.boolGetSP("localIPActived");
        pinSavedSP = spClass.strGetSP("systemIPPIN");

        btnSelectIPLocal.setOnClickListener(view -> {

            if(editTxtConnectionIPLocal.getText().toString().equals("") || editTxtConnectionIPLocal.getText().toString().equals("ND")){
                baseApp.showToast("Para poder cambiar la IP a la Local, escribe algo antes en el campo.");
            }else {
                strIP = editTxtConnectionIPLocal.getText().toString();
                spClass.intSetSP("IPConnectionSelected", 1);
                baseApp.showSnackBar("Ahora est??s conectado con la IP Local");
                saveDataConnection("");
                refreshData();
            }
        });

        btnSelectIPExterna.setOnClickListener(view -> {

            if(editTxtConnectionIPExterna.getText().toString().equals("") || editTxtConnectionIPExterna.getText().toString().equals("ND")){
                baseApp.showToast("Para poder cambiar la IP a la Externa, escribe algo antes en el campo.");
            }else {
                strIP = editTxtConnectionIPExterna.getText().toString();
                spClass.intSetSP("IPConnectionSelected", 1);
                baseApp.showSnackBar("Ahora est??s conectado con la IP Externa");
                saveDataConnection("");
                refreshData();
            }
        });


        editTxtConnectionIPLocal.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetGUIData();
            }
        });

        editTxtConnectionIPExterna.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetGUIData();
            }
        });

        switchIPLocal.setChecked(localIPActived);

        switchIPLocal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            localIPActived = isChecked;
            spClass.boolSetSP("localIPActived", isChecked);
            visibiityGUI();
        });

        btnChangeConnection.setOnClickListener(v -> saveDataConnection(""));
        btnCheckConnection.setOnClickListener(v -> checkConnection());

        refreshData();
        visibiityGUI();
    }

    public void visibiityGUI(){

        if(!localIPActived){
            txtTitleConnection.setText("Modifica los datos de conexi??n al Servidor a continuaci??n. \n \nEscribe la IP Externa en el campo de a continuaci??n. La puedes solicitar con el departamento de Sistemas u obtenerla desde la funci??n 'Obtener datos desde el Sistema' en el men?? '...' de la derecha superior");
            editTxtConnectionIPLocal.setVisibility(View.GONE);
            txtTitleIPLocal.setVisibility(View.GONE);
            txtIPCurrent.setVisibility(View.GONE);
            layoutButtonsSelect.setVisibility(View.GONE);
        }else{
            txtTitleConnection.setText("Modifica los datos de conexi??n al Servidor a continuaci??n. \n \nCon??ctate con la IP Local cuando te encuentres conectado a la misma red que el Servidor o IP Externa cuando est??s conectado a otra red.");
            editTxtConnectionIPLocal.setVisibility(View.VISIBLE);
            txtTitleIPLocal.setVisibility(View.VISIBLE);
            txtIPCurrent.setVisibility(View.VISIBLE);
            layoutButtonsSelect.setVisibility(View.VISIBLE);
        }

        editTxtConnectionDataBase.setVisibility(View.GONE);
        editTxtConnectionUser.setVisibility(View.GONE);
        editTxtConnectionPassword.setVisibility(View.GONE);
    }


    @SuppressLint("ResourceAsColor")
    public void refreshData(){

        editTxtConnectionIPLocal.setText(spClass.strGetSP("IPConnectionLocal"));
        editTxtConnectionIPExterna.setText(spClass.strGetSP("IPConnectionExterna"));
        txtIPCurrent.setText("IP Actual: " + strIP);

        //if(spClass.intGetSP("IPConnectionSelected") == 1){ //local seleccionado
        //    btnSelectIPLocal.setBackgroundColor(R.color.colorVerde);
        //    btnSelectIPExterna.setBackgroundColor(R.color.colorAccent);
        //}else if(spClass.intGetSP("IPConnectionSelected") == 2){ //externa seleccionado
        //    btnSelectIPLocal.setBackgroundColor(R.color.colorAccent);
        //    btnSelectIPExterna.setBackgroundColor(R.color.colorVerde);
        //}else{
        //    btnSelectIPLocal.setBackgroundColor(R.color.colorAccent);
        //    btnSelectIPExterna.setBackgroundColor(R.color.colorAccent);
        //}
    }

    public void resetGUIData(){
        spClass.intSetSP("IPConnectionSelected", 0);
        txtIPCurrent.setText("IP Actual: Selecciona de nuevo a cual IP deseas conectarte debido a que los datos de los campos han sido cambiados.");
    }

    public void checkConnection(){
        saveDataConnection("checkConnection");
        progressDialog = new ProgressDialog(ChangeConnectionActivity.this);
        progressDialog.setMessage("Espera un momento por favor..."); // Setting Message
        progressDialog.setTitle("Revisando conexi??n al Servidor"); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cerrar alerta", (dialog, which) -> progressDialog.dismiss());
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cerrar aplicaci??n", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //>= API 16
                finishAffinity();
            }
        });
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        new Thread(() -> {
            Looper.prepare();
            try {

                ConnectionClass connectionClass = new ConnectionClass(getApplicationContext());

                if (connectionClass.ConnectionMDS() != null) {
                    failConnection = false;
                    progressDialog.dismiss();
                    baseApp.showSnackBar("Conexi??n exitosa");

                } else {
                    failConnection = true;
                    progressDialog.dismiss();

                }

            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
                baseApp.showSnackBar("Ocurri?? el error" + e);
            }
        }).start();

        progressDialog.setOnDismissListener(dialog -> {
            if(failConnection){
                 baseApp.showAlert("Error", "No se pudo establecer conexi??n con el Servidor");
            }
        });
    }

    public void saveDataConnection(String callMethod){
        String ip, ipOld, db, user, password;

        spClass.strSetSP("IPConnectionLocal", editTxtConnectionIPLocal.getText().toString());
        spClass.strSetSP("IPConnectionExterna", editTxtConnectionIPExterna.getText().toString());

        ipOld = SPClass.strGetSP("IPConnection");

        if(localIPActived) {
            ip = strIP;
        }else{
            ip = editTxtConnectionIPExterna.getText().toString();
        }

        db  = editTxtConnectionDataBase.getText().toString();
        user = editTxtConnectionUser.getText().toString();
        password = editTxtConnectionPassword.getText().toString();

        if(!ipOld.equals(ip)){

            spClass.strSetSP("IPConnection", ip);
            spClass.strSetSP("DBConnection", db);
            spClass.strSetSP("UserConnection", user);
            spClass.strSetSP("PasswordConnection", password);

            baseApp.showToast("Los nuevos datos de conexi??n han sido guardados");

        }else if(ipOld.equals(ip) && callMethod.equals("")){
            baseApp.showToast("Todo ha sido guardado");
        }
    }

    public void getIPApi(){
        final String ip;

        Call<WResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .getConnectionData();

        call.enqueue(new Callback<WResponse>() {
            @Override
            public void onResponse(Call<WResponse> call, retrofit2.Response<WResponse> response) {

                if (response.body() != null) {
                    WResponse wResponse = response.body();
                    editTxtConnectionIPExterna.setText(wResponse.getIp_externa());
                    editTxtConnectionIPLocal.setText(wResponse.getIp_local());
                    saveDataConnection("getIPApi");
                }
            }
            @Override
            public void onFailure(Call<WResponse> call, Throwable t) {
                baseApp.showAlert("Error", "Ocurri?? el error al cargar la ip del Servidor: " + t.getMessage());
            }

        });
    }

    public void verifyPIN(boolean checkFormPin, boolean clickedInGetSystemData){

        Call<WResponse> call = RetrofitClient
                .getInstance()
                .getApi()
                .getConnectionData();

        call.enqueue(new Callback<WResponse>() {
            @Override
            public void onResponse(Call<WResponse> call, retrofit2.Response<WResponse> response) {

                if (response.body() != null) {
                    WResponse wResponse = response.body();
                    PINUnblockSystemIP = wResponse.getPin();

                    if(checkFormPin) {
                        checkPIN();
                    }else {
                        if(!clickedInGetSystemData){
                            checkPINNotChanged(false);
                        }
                    }

                    if(clickedInGetSystemData){
                        checkPINNotChanged(true);
                    }
                }
            }
            @Override
            public void onFailure(Call<WResponse> call, Throwable t) {
                baseApp.showAlert("Error", "Ocurri?? el error al cargar la ip del Servidor: " + t.getMessage());
            }

        });
    }

    public void showMenuBottomPIN(){
        menuBottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        menuBottomSheet.setContentView(R.layout.bottom_sheet_pin_unblock);
        menuBottomSheet.setTitle("Desbloquear funci??n");
        menuBottomSheet.show();

        editTxtPIN = menuBottomSheet.findViewById(R.id.editTxtPIN);

        btnOk = menuBottomSheet.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> verifyPIN(true, false));
    }

    public void closeMenuBottomPIN(){
        try {
            if (menuBottomSheet != null) {
                menuBottomSheet.dismiss();
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurri?? un error inesperado, " + ex);
        }
    }

    public void checkPIN(){

        String PINEditTxt;
        PINEditTxt = editTxtPIN.getText().toString();

        if(PINEditTxt.equals(PINUnblockSystemIP)){

            spClass.strSetSP("systemIPPIN", PINEditTxt);
            spClass.boolSetSP("unBlockedSystemIP", true);
            unBlockedSystemIP = true;

            closeMenuBottomPIN();
            baseApp.showToast("PIN verificado, ya puedes utilizar la herramienta.");
            verifyPIN(false, true);

        }else{
            baseApp.showToast("El PIN no coincide.");
        }
    }

    public void checkPINNotChanged(boolean clickedInGetSystemData){

        pinSavedSP = spClass.strGetSP("systemIPPIN");

            if (!PINUnblockSystemIP.equals(pinSavedSP)) {
                //baseApp.showToast("antes: " + pinSavedSP + ", ahora: " + PINUnblockSystemIP);
                baseApp.showAlertDialog("warning", "Una funci??n se ha bloqueado", "Esta funci??n se bloque?? ya que el PIN de verificaci??n cambi??.", true);

                spClass.strSetSP("systemIPPIN", "ND");
                spClass.boolSetSP("unBlockedSystemIP", false);
                unBlockedSystemIP = false;

                }else{
                    if(clickedInGetSystemData) {
                        getIPApi();
                    }
            }
    }

    public void onBackPressed() {
        if(baseApp.returnSessionVerify()){
            functionsapp.goMainActivity();
        }else{
            functionsapp.goLoginActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_change_connection, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == android.R.id.home) {
            if(baseApp.returnSessionVerify()){
                functionsapp.goMainActivity();
            }else{
                functionsapp.goLoginActivity();
            }
        }else if(id == R.id.menu_option_system){
            if(!unBlockedSystemIP){
                showMenuBottomPIN();
            }else {
                verifyPIN(false, true);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}


