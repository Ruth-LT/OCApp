package com.mds.ocapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mds.ocapp.R;
import com.mds.ocapp.application.BaseApp;
import com.mds.ocapp.application.ConnectionClass;
import com.mds.ocapp.application.FunctionsApp;
import com.mds.ocapp.application.SPClass;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {

    Boolean failConnection = false;

    ProgressDialog progressDialog;
    ProgressBar progressBarLogin;
    EditText editTxtUser, editTxtPassword;
    Button btnLogin;
    TextView txtName, btnGoConfigurationServer;
    ImageView imgLogo;

    BaseApp baseApp = new BaseApp(this);
    FunctionsApp functionsapp = new FunctionsApp(this);
    SPClass spClass = new SPClass(this);

    private static final int MULTIPLE_PERMISSIONS_REQUEST_CODE = 3;
    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,};

    MediaPlayer mp;
    int clicksEgg = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("");

        baseApp.setUpRealmConfig();
        baseApp.sessionVerify();

        editTxtUser = findViewById(R.id.editTxtUser);
        editTxtPassword = findViewById(R.id.editTxtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        txtName = findViewById(R.id.txtName);
        imgLogo = findViewById(R.id.imgLogo);

        btnLogin.setOnClickListener(v -> executeMethodServer("verifyLogin"));

        if (spClass.boolGetSP("ipChanged")) {
            spClass.deleteSP("ipChanged");
            baseApp.showAlert("Alerta", "Ha habido un reciente cambio de Ip del Servidor, por seguridad, tu cuenta ha sido cerrada. Por favor, ingresa de nuevo tus datos.");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        }

        editTxtUser.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                //executeMethodServer("setNamePerson");
                editTxtPassword.requestFocus();
                return true;
            }
            return false;
        });

        // editTxtPassword.setOnClickListener(v -> executeMethodServer("setNamePerson"));
        editTxtPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                executeMethodServer("verifyLogin");
                return true;
            }
            return false;
        });

        if(spClass.strGetSP("IPConnection").equals("ND")){
            showMenuBottomNoServer();
        }

        imgLogo.setOnClickListener(v -> {
            clicksEgg++;
            verifyEgg();
        });

        mp = MediaPlayer.create(this, R.raw.egg);
        setGreet();
    }

    public void executeMethodServer(String method) {

        try {
            if (!baseApp.checkConnection()) {
                baseApp.showToast("No tienes acceso a Internet, verifica tu conexión");
            } else {

                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Espera un momento... Si ésto tarda demasiado, haz clic fuera de la alerta para cerrarla.");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                //progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cambiar configuración", (dialog, which) -> functionsapp.goChangeConnection());
                //progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cerrar aplicación", (dialog, which) -> {
                //    //>= API 16
                //    finishAffinity();
                //});
                progressDialog.setCancelable(true);
                progressDialog.show(); // Display Progress Dialog

                new Thread(() -> {
                    Looper.prepare();
                    try {
                        ConnectionClass connectionClass = new ConnectionClass(getApplicationContext());

                        if (connectionClass.ConnectionMDS() != null) {
                            progressDialog.dismiss();


                            switch (method) {
                                case "setNamePerson":
                                    setNamePerson();
                                    break;
                                case "verifyLogin":
                                    verifyLogin();
                                    break;
                                default:
                                    return;
                            }
                            failConnection = false;
                        } else {
                            progressDialog.dismiss();
                            //
                            failConnection = true;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        baseApp.showToast("Ocurrió el error" + e);
                    }
                    progressDialog.dismiss();
                    //
                }).start();

                Thread.sleep(400);
            }

            if (progressDialog != null) {
                progressDialog.setOnDismissListener(dialog -> {
                    if (failConnection) {
                        baseApp.showAlertDialog("error", "Error", "No se pudo establecer conexión con el Servidor", true);
                    }

                    Thread.interrupted();
                });
            }

        } catch (Exception ex) {
            baseApp.showToast("Ocurrió un error inesperado, " + ex);
        }
    }

    public void verifyLogin() {
        final SPClass spClass = new SPClass(this);
        boolean booleanSuccess = false;
        String messageServer = "";
        PreparedStatement loComando = null;

        String lcUsuario = editTxtUser.getText().toString();
        String lcContrasena = editTxtPassword.getText().toString();

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (lcUsuario.length() == 0) {
            baseApp.showSnackBar("Por favor, escribe un usuario");
        } else if (lcContrasena.length() == 0) {
            baseApp.showSnackBar("Por favor, escribe una contraseña");
        } else{

            loComando = baseApp.execute_SP("EXECUTE Login_Web_Android ?, ?");

            if (loComando == null) {
                baseApp.showSnackBar("Error al Crear SP Login_Web_Android");
            } else {
                try {
                    int lnUsuario = Integer.parseInt(lcUsuario);
                    loComando.setInt(1, lnUsuario);
                    loComando.setString(2, lcContrasena);

                    ResultSet Datos = loComando.executeQuery();

                    while (Datos.next()) {
                        booleanSuccess = Datos.getBoolean("exito");
                        messageServer  = Datos.getString("mensaje");
                        spClass.strSetSP("name", Datos.getString("nombre_persona").trim());
                        spClass.intSetSP("sucursal", Datos.getInt("sucursal"));
                        spClass.boolSetSP("isSupervisor", Datos.getBoolean("es_supervisor"));
                    }
                } catch (Exception ex) {
                    baseApp.showSnackBar("Error en el Login Web");
                }
            }

            if (booleanSuccess) {
                functionsapp.goMainActivity();
                spClass.intSetSP("user", Integer.parseInt(lcUsuario));
            } else {
                baseApp.showSnackBar(messageServer);
                //
            }
        }

    }

    public void setNamePerson() {
        try {
            ConnectionClass connectionClass = new ConnectionClass(getApplicationContext());

            if (connectionClass.ConnectionMDS() != null) {
                PreparedStatement loComando = baseApp.execute_SP("EXECUTE Trae_Nombre_Persona_Android ?");

                String userTxt = editTxtUser.getText().toString();
                String personName;

                if (userTxt.length() == 0) {
                    baseApp.showToast("Por favor, escribe un usuario");
                    setGreet();
                } else if (loComando == null) {
                    baseApp.showSnackBar("Error al Crear SP Trae_Nombre_Persona_Android");
                } else {
                    try {
                        int lnUser = Integer.parseInt(userTxt);
                        loComando.setInt(1, lnUser);

                        ResultSet Datos = loComando.executeQuery();

                        while (Datos.next()) {
                            personName = Datos.getString("nombre_persona");
                            if (personName.isEmpty()) {
                                setGreet();
                            } else {
                                txtName.setText("Hola, " + personName);
                            }
                        }
                    } catch (Exception ex) {
                        baseApp.showToast("Error al cargar el nombre de la persona");
                    }
                }
            } else {
                progressDialog.dismiss();
                //
                failConnection = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            baseApp.showToast("Ocurrió el error" + e);
        }
    }

    public void setGreet() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (hourOfDay >= 5 && hourOfDay < 12) {
            txtName.setText("Hola, buenos días");
        } else if (hourOfDay >= 12 && hourOfDay < 19) {
            txtName.setText("Hola, buenas tardes");
        } else if (hourOfDay >= 19) {
            txtName.setText("Hola, buenas noches");
        }
    }

    public void showMenuBottomNoServer(){
        BottomSheetDialog menuBottomSheet;

        menuBottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        menuBottomSheet.setContentView(R.layout.bottom_sheet_no_server);
        menuBottomSheet.setCancelable(false);
        menuBottomSheet.show();

        btnGoConfigurationServer = menuBottomSheet.findViewById(R.id.btnGoConfigurationServer);
        btnGoConfigurationServer.setOnClickListener(v-> functionsapp.goChangeConnection());
    }

    public void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(LoginActivity.this, permissions[1]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(LoginActivity.this, permissions[2]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(LoginActivity.this, permissions[3]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(LoginActivity.this, permissions[4]) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LoginActivity.this, permissions, MULTIPLE_PERMISSIONS_REQUEST_CODE);
        }
    }

    public void verifyEgg(){

        try {

            if(clicksEgg > 9) {

                if (!mp.isPlaying()) {
                    mp.start();

                    baseApp.showToast("SIA");
                    clicksEgg = 0;
                }
            }

        }catch (Exception ex){
            baseApp.showToast("ERRORR!!!!: " + ex);
        }
    }
    public void onResume() {
        super.onResume();
        setGreet();
    }

    public void onPause(){
        super.onPause();
        mp.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_option_settings:
                functionsapp.goChangeConnection();
                return true;
            case R.id.menu_option_about:
                functionsapp.goAboutActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}