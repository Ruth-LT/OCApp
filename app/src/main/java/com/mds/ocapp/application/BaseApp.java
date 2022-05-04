package com.mds.ocapp.application;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.mds.ocapp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseApp extends Application {

    private Realm realm;
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    private static final String DECIMAL_FORMAT = "#,###,###.##";
    String DIRECTORY_BACKUPS = "DiCampoOC/Databases";

    public static final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static Random RANDOM = new Random();

    private static Context context;
    int orientation, nextID;

    ProgressDialog barBackup;

    private final String[] UNIDADES = {"", "un ", "dos ", "tres ", "cuatro ", "cinco ", "seis ", "siete ", "ocho ", "nueve "};
    private final String[] DECENAS = {"diez ", "once ", "doce ", "trece ", "catorce ", "quince ", "dieciseis ",
            "diecisiete ", "dieciocho ", "diecinueve", "veinte ", "treinta ", "cuarenta ",
            "cincuenta ", "sesenta ", "setenta ", "ochenta ", "noventa "};
    private final String[] CENTENAS = {"", "ciento ", "doscientos ", "trecientos ", "cuatrocientos ", "quinientos ", "seiscientos ",
            "setecientos ", "ochocientos ", "novecientos "};


    public BaseApp(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    public void setUpRealmConfig() {

        try {
            Realm.init(context);
            RealmConfiguration realmConfig = new RealmConfiguration
                    .Builder()
                    .name("abpollo.realm")
                    .schemaVersion(1)
                    .deleteRealmIfMigrationNeeded() // todo remove for production
                    //.migration(new RealmMigrations(context))
                    .build();
            Realm.setDefaultConfiguration(realmConfig);

        }catch (Exception ex){
            showAlert("Error", "Reporta el siguiente error al dpto de Sistemas: " + ex);
        }
    }

    public boolean isSuperUser(){
        com.mds.ocapp.application.SPClass spClass = new com.mds.ocapp.application.SPClass(context);

        if (spClass.intGetSP("user") == 2){
            return true;
        }else{
            return false;
        }
    }

    // función para verificar si hay una sesión creada
    public void sessionVerify() {
        com.mds.ocapp.application.SPClass spClass = new com.mds.ocapp.application.SPClass(context);
        com.mds.ocapp.application.FunctionsApp functionsApp = new com.mds.ocapp.application.FunctionsApp(context);

        try {
            if (spClass.intGetSP("user") != 0) {
                functionsApp.goMainActivity();
            }
        }catch (Exception ex){
            logoutForced();
        }
    }

    // función para retornar si hay una sesión creada
    public boolean returnSessionVerify() {
        com.mds.ocapp.application.SPClass spClass = new com.mds.ocapp.application.SPClass(context);

        if (spClass.intGetSP("user") == 0) {
            return false;
        } else {
            return true;
        }
    }

    // función para verificar si hay una ..
    public void inVisitVerify() {
        com.mds.ocapp.application.SPClass spClass = new com.mds.ocapp.application.SPClass(context);
        com.mds.ocapp.application.FunctionsApp functionsApp = new com.mds.ocapp.application.FunctionsApp(context);
        int nClient = spClass.intGetSP("nClient");

        if (spClass.boolGetSP("inVisit")) {
            //functionsApp.goClientActivity(nClient);
        }
    }

    // función para retornar si hay una visita actual
    public boolean returnInSession() {
        com.mds.ocapp.application.SPClass spClass = new com.mds.ocapp.application.SPClass(context);

        if (spClass.boolGetSP("inVisit")) {
            return true;
        } else {
            return false;
        }
    }

    // logout
    public void logout() {
        com.mds.ocapp.application.FunctionsApp functionsApp = new com.mds.ocapp.application.FunctionsApp(context);

        new AlertDialog.Builder(context)
                .setMessage("¿Estás seguro que quieres cerrar tu sesión actual? Se perderán todos los datos no enviados.")
                .setCancelable(true)
                .setPositiveButton("Sí", (dialog, id) -> {
                    cleanAllSP();
                    cleanAllData();
                    functionsApp.goLoginActivity();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // logout
    public void logoutForced() {
        cleanAllSP();
        cleanAllData();
        //goLoginActivity();
    }

    // borrar todos los datos de SP
    public void cleanAllSP() {
        com.mds.ocapp.application.SPClass spClass = new com.mds.ocapp.application.SPClass(context);
        try {
            spClass.deleteSP("user");
            spClass.deleteSP("name");
            spClass.deleteSP("date_last_sync");
            spClass.deleteSP("errors_last_sync");
            spClass.deleteSP("inVisit");
            spClass.deleteSP("nClient");
            spClass.deleteSP("nList");
            spClass.deleteSP("nVisit");
            spClass.deleteSP("sPaymentMethod");
            spClass.deleteSP("bAlertDetailsOrder");

            spClass.deleteSP("inRoute");
            spClass.deleteSP("inventoryLoaded");
            spClass.deleteSP("idRoute");
            //spClass.deleteSP("ipChanged");
        }catch (Exception ex){
            showToast("Ocurrió un error inesperado");
        }

    }

    // borrar todos los datos de Realm
    public void cleanAllData() {
        realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        //realm.delete(Lists.class);
        //realm.delete(ListClients.class);
        //realm.delete(Articles.class);
        //realm.delete(SubLines.class);
        //realm.delete(VisitsClients.class);
        //realm.delete(DetailsOrders.class);
        //realm.delete(Orders.class);
        realm.commitTransaction();
    }

    public void deleteData() {
        com.mds.ocapp.application.SPClass spClass = new com.mds.ocapp.application.SPClass(context);

        spClass.deleteSP("date_last_sync");
        spClass.deleteSP("errors_last_sync");
        spClass.deleteSP("inVisit");
        spClass.deleteSP("nClient");
        spClass.deleteSP("nList");

        //VisitsClients visits;

        try {
            realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            //realm.delete(Articles.class);
            //realm.delete(DetailsOrders.class);
            //realm.delete(ListClients.class);
            //realm.delete(Lists.class);
            //realm.delete(Orders.class);
            //realm.delete(Prices.class);
            //realm.delete(SubLines.class);
            //realm.delete(VisitsClients.class);
            //realm.delete(MapRoutes.class);
            //realm.delete(TopArticles.class);

            realm.commitTransaction();

           //RealmResults<VisitsClients> visitsClients = realm.where(VisitsClients.class).equalTo("fecha_visita_fin", "").findAll();
           //int totalVisits = visitsClients.size();

           //if (totalVisits > 0) {
           //    for (int i = 0; i < totalVisits; i++) {
           //        visits = visitsClients.get(i);
           //        realm.beginTransaction();
           //        visits.setVisitada(true);
           //        visits.setFinalizado_forzado(true);
           //        visits.setFecha_visita_fin(getCurrentDateFormated());
           //        realm.insertOrUpdate(visits);
           //        realm.commitTransaction();
           //    }
           //}

            showSnackBar("Se han eliminado todos los datos");
        } catch (Exception ex) {
            showToast("Ocurrió un error al borrar los Datos, reporta el siguiente error al dpto de Sistemas: " + ex);
        }
    }


    // funcion para Toast
    public void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    // funcion para Toast con gravity bottom

    public void showToast2(String text){
        Toast toast= Toast.makeText(context,
                text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER| Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public void showLog(String text) {
        Log.i("Proceso App", text);
    }

    public void showSnackBar(String text) {
        try {
            Snackbar.make(((Activity) (context)).findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                    .show();
        }catch (Exception ex){
            showAlert("Error", "Ocurrió un error al mostrar un SnackBar, error: "  + ex);
        }
    }

    // mostrar una alerta con animacion
    public void showAlertDialog(String type, String title, String message, boolean isCancelable){
        switch(type){

            case "info":
                new SweetAlertDialog(context)
                        .setTitleText(title)
                        .setContentText(message)
                        .show();
                break;

            case "error":
                SweetAlertDialog sweetAlertError = new SweetAlertDialog(context,SweetAlertDialog.ERROR_TYPE);
                sweetAlertError.setTitleText(title);
                sweetAlertError.setContentText(message);
                if(isCancelable){
                    sweetAlertError.setCancelable(true);
                }else{
                    sweetAlertError.setCancelable(false);
                }
                sweetAlertError.show();
                Button btnAlertDialogError = sweetAlertError.findViewById(cn.pedant.SweetAlert.R.id.confirm_button);
                btnAlertDialogError.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                btnAlertDialogError.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                break;

            case "warning":
                SweetAlertDialog sweetAlertWarning = new SweetAlertDialog(context,SweetAlertDialog.WARNING_TYPE);
                sweetAlertWarning.setTitleText(title);
                sweetAlertWarning.setContentText(message);
                if(isCancelable){
                    sweetAlertWarning.setCancelable(true);
                }else{
                    sweetAlertWarning.setCancelable(false);
                }
                sweetAlertWarning.show();

                Button btnAlertDialogWarning = sweetAlertWarning.findViewById(cn.pedant.SweetAlert.R.id.confirm_button);
                btnAlertDialogWarning.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                btnAlertDialogWarning.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                break;

            case "success":
                SweetAlertDialog sweetAlertSuccess = new SweetAlertDialog(context,SweetAlertDialog.SUCCESS_TYPE);
                sweetAlertSuccess.setTitleText(title);
                sweetAlertSuccess.setContentText(message);
                if(isCancelable){
                    sweetAlertSuccess.setCancelable(true);
                }else{
                    sweetAlertSuccess.setCancelable(false);
                }
                sweetAlertSuccess.show();

                Button btnAlertDialogSuccess = sweetAlertSuccess.findViewById(cn.pedant.SweetAlert.R.id.confirm_button);
                btnAlertDialogSuccess.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                btnAlertDialogSuccess.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                break;

            default:
                SweetAlertDialog sweetAlertDefault = new SweetAlertDialog(context);
                sweetAlertDefault.setTitleText(title);
                sweetAlertDefault.setContentText(message);
                if(isCancelable){
                    sweetAlertDefault.setCancelable(true);
                }else{
                    sweetAlertDefault.setCancelable(false);
                }
                sweetAlertDefault.show();

                Button btnAlertDialogDefault = sweetAlertDefault.findViewById(cn.pedant.SweetAlert.R.id.confirm_button);
                btnAlertDialogDefault.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                btnAlertDialogDefault.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                break;

        }
    }

    public void setTextView(View vista, String text) {
        try {
            TextView txtValue = (TextView) vista;
            txtValue.setText(text);
        } catch (Exception ex) {
            showAlert("Error", "Ocurrió un error al colocar un texto en un TextView, error: " + ex);
        }
    }

    public boolean verifyValueArray(String table, String value){

       // boolean flag = false;
//
       // try {
       //     if (arrayTables.contains(table)) {
//
       //         switch(table){
//
       //             case "images":
       //                 if (arrayImagesTables.contains(value)) {
       //                     flag = true;
       //                 }else{
       //                     flag = false;
       //                 }
       //             break;
//
       //             case "prices":
       //                 if (arrayPricesFields.contains(value)) {
       //                     flag = true;
       //                 }else{
       //                     flag = false;
       //                 }
       //         }
//
       //     } else {
       //         showAlert("Error", "Se está intentando verificar un valor de un Array que no es permitido");
       //     }
       // }catch (Exception ex){
       //     showAlert("Error Interno", "Ocurrió un error interno, repórtalo: " + ex);
       // }
//
       // return flag;
        return true;
    }

    public void showKeyboardEditText(EditText field) {

        field.setFocusable(true);
        field.setFocusableInTouchMode(true);
        field.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(context.getApplicationContext().INPUT_METHOD_SERVICE);
        imm.showSoftInput(field, InputMethodManager.SHOW_IMPLICIT);

    }

    public void closeKeyboard() {
        View view = ((Activity) (context)).getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public Date getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public String getCurrentDateFormated() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = dateFormat.format(date);

        return strDate;
    }

    public String getCurrentDateFormated2() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String strDate = dateFormat.format(date);

        return strDate;
    }

    public String getCurrentDateFlat() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String strDate = dateFormat.format(date);

        return strDate;
    }

    // funcion para convertir un date a string
    public String convertDateString(Date dateDate) {
        String dateString = null;
        SimpleDateFormat sdfr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /*you can also use DateFormat reference instead of SimpleDateFormat
         * like this: DateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
         */
        try {
            dateString = sdfr.format(dateDate);
        } catch (Exception e) {
            //
        }
        return dateString;
    }

    // funcion para convertir un date a string
    public String convertDateString2(Date dateDate) {
        String dateString = null;
        SimpleDateFormat sdfr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /*you can also use DateFormat reference instead of SimpleDateFormat
         * like this: DateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
         */
        try {
            dateString = sdfr.format(dateDate);
        } catch (Exception e) {
            //
        }
        return dateString;
    }

    // funcion para convertir un date a string
    public String convertDateStringWOTime(Date dateDate) {
        String dateString = null;
        SimpleDateFormat sdfr = new SimpleDateFormat("dd MMM yyyy");
        /*you can also use DateFormat reference instead of SimpleDateFormat
         * like this: DateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
         */
        try {
            dateString = sdfr.format(dateDate);
        } catch (Exception e) {
            //
        }
        return dateString;
    }


    // funcion para convertir un string a date
    public Date convertDate(String dateString) {
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;

    }

    @SuppressLint("StringFormatMatches")
    public String dateFormat(Date dateStrn) {

        long date = dateStrn.getTime();
        long now = getCurrentDate().getTime();

        final long diff = now - date;

        try {
            if (date >= now) {
                return context.getResources().getString(R.string.text_date_future);
            } else if (diff < 10000) {
                return context.getResources().getString(R.string.text_date_rightnow);
            } else if (diff >= 10000 && diff < 60000) {
                String strFormaxSeconds = context.getResources().getString(R.string.text_date_xseconds);
                return String.format(strFormaxSeconds, (diff / 1000));
                //return "Hace " + (diff/1000) + "segundos";
            } else if (diff >= 60000 && diff < 120000) {
                return context.getResources().getString(R.string.text_date_minute);
                //return "Hace " + (diff/60000) + "minuto";
            } else if (diff >= 120000 && diff < 3600000) {
                String strFormaxMinutes = context.getResources().getString(R.string.text_date_xminutes);
                return String.format(strFormaxMinutes, (diff / 60000));
                //return "Hace " + (diff/60000) + "minutos";
            } else if (diff >= 3600000 && diff < 7200000) {
                //return "Hace " + (diff/3600000) + "hora";
                return context.getResources().getString(R.string.text_date_hour);
            } else if (diff >= 7200000 && diff < 86400000) {
                String strFormaxHours = context.getResources().getString(R.string.text_date_xhours);
                return String.format(strFormaxHours, (diff / 3600000));
                //return "Hace " + (diff/3600000) + "horas";
            } else if (diff >= 86400000 && diff < 172800000) {
                return context.getResources().getString(R.string.text_date_yesterday);
                //return "Hace " + (diff/86400000) +  "día";
            } else if (diff >= 172800000 && diff < 604800000) {
                String strFormaxDays = context.getResources().getString(R.string.text_date_xdays);
                return String.format(strFormaxDays, (diff / 86400000));
                //return "Hace "  + (diff/86400000) + "días";
            } else if (diff >= 604800000 && diff < 1209600000) {
                return context.getResources().getString(R.string.text_date_lastweek);
                //return "Hace " + (diff/604800000) + " semana";
            } else if (diff >= 1209600000 && diff < 2628002880L) {
                String strFormaxWeeks = context.getResources().getString(R.string.text_date_xweeks);
                return String.format(strFormaxWeeks, (diff / 604800000));
                //return "Hace " + (diff/604800000) + "semanas";
            } else if (diff >= 2628002880L && diff < 5256005760L) {
                return context.getResources().getString(R.string.text_date_lastmonth);
                //return "Hace " + (diff/2628002880L) + "mes";
            } else if (diff >= 5256005760L && diff < 31536000000L) {
                String strFormaxMonths = context.getResources().getString(R.string.text_date_xmonths);
                return String.format(strFormaxMonths, (diff / 2628002880L));
                //return "Hace " + (diff/2628002880L) + "meses";
            } else if (diff >= 31536000000L && diff < 63072000000L) {
                return context.getResources().getString(R.string.text_date_lastyear);
                //return "Hace " + (diff/31536000000L) +  " año";
            } else if (diff >= 63072000000L) {
                String strFormaxYears = context.getResources().getString(R.string.text_date_xyears);
                return String.format(strFormaxYears, (diff / 31536000000L));
                //return "Hace " + (diff/31536000000L) + " años";
            } else {
                // falta convertir en local
                String mydate = dateStrn.toString();
                SimpleDateFormat src = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH);
                SimpleDateFormat dest = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
                try {
                    dateStrn = src.parse(mydate);
                } catch (ParseException e) {
                    Log.d("Exception", e.getMessage());
                }
                String result = dest.format(date);
                return result;
            }
        } catch (Exception e) {
            showToast("Error while load date: " + e);
            return "Error";
        }
    }

    public String dateFormatTwoDates(Date dateStrn, Date dateStrn2) {

        long date = dateStrn.getTime();
        long now = dateStrn2.getTime();

        final long diff = now - date;

        try {
            if (date >= now) {
                return context.getResources().getString(R.string.text_date_future);
            } else if (diff < 10000) {
                return context.getResources().getString(R.string.text_date_rightnow);
            } else if (diff >= 10000 && diff < 60000) {
                String strFormaxSeconds = context.getResources().getString(R.string.text_date_xseconds2);
                return String.format(strFormaxSeconds, (diff / 1000));
                //return "Hace " + (diff/1000) + "segundos";
            } else if (diff >= 60000 && diff < 120000) {
                return context.getResources().getString(R.string.text_date_minute2);
                //return "Hace " + (diff/60000) + "minuto";
            } else if (diff >= 120000 && diff < 3600000) {
                String strFormaxMinutes = context.getResources().getString(R.string.text_date_xminutes2);
                return String.format(strFormaxMinutes, (diff / 60000));
                //return "Hace " + (diff/60000) + "minutos";
            } else if (diff >= 3600000 && diff < 7200000) {
                //return "Hace " + (diff/3600000) + "hora";
                return context.getResources().getString(R.string.text_date_hour);
            } else if (diff >= 7200000 && diff < 86400000) {
                String strFormaxHours = context.getResources().getString(R.string.text_date_xhours2);
                return String.format(strFormaxHours, (diff / 3600000));
                //return "Hace " + (diff/3600000) + "horas";
            } else if (diff >= 86400000 && diff < 172800000) {
                return context.getResources().getString(R.string.text_date_yesterday);
                //return "Hace " + (diff/86400000) +  "día";
            } else if (diff >= 172800000 && diff < 604800000) {
                String strFormaxDays = context.getResources().getString(R.string.text_date_xdays2);
                return String.format(strFormaxDays, (diff / 86400000));
                //return "Hace "  + (diff/86400000) + "días";
            } else if (diff >= 604800000 && diff < 1209600000) {
                return context.getResources().getString(R.string.text_date_lastweek);
                //return "Hace " + (diff/604800000) + " semana";
            } else if (diff >= 1209600000 && diff < 2628002880L) {
                String strFormaxWeeks = context.getResources().getString(R.string.text_date_xweeks2);
                return String.format(strFormaxWeeks, (diff / 604800000));
                //return "Hace " + (diff/604800000) + "semanas";
            } else if (diff >= 2628002880L && diff < 5256005760L) {
                return context.getResources().getString(R.string.text_date_lastmonth);
                //return "Hace " + (diff/2628002880L) + "mes";
            } else if (diff >= 5256005760L && diff < 31536000000L) {
                String strFormaxMonths = context.getResources().getString(R.string.text_date_xmonths2);
                return String.format(strFormaxMonths, (diff / 2628002880L));
                //return "Hace " + (diff/2628002880L) + "meses";
            } else if (diff >= 31536000000L && diff < 63072000000L) {
                return context.getResources().getString(R.string.text_date_lastyear);
                //return "Hace " + (diff/31536000000L) +  " año";
            } else if (diff >= 63072000000L) {
                String strFormaxYears = context.getResources().getString(R.string.text_date_xyears2);
                return String.format(strFormaxYears, (diff / 31536000000L));
                //return "Hace " + (diff/31536000000L) + " años";
            } else {
                // falta convertir en local
                String mydate = dateStrn.toString();
                SimpleDateFormat src = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH);
                SimpleDateFormat dest = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
                try {
                    dateStrn = src.parse(mydate);
                } catch (ParseException e) {
                    Log.d("Exception", e.getMessage());
                }
                String result = dest.format(date);
                return result;
            }
        } catch (Exception e) {
            showToast("Error while load date: " + e);
            return "Error";
        }
    }

//    public String calculeAge(Calendar fechaNac) {
//        int months = 0;
//
//        Calendar today = Calendar.getInstance();
//        int diffYear = today.get(Calendar.YEAR) - fechaNac.get(Calendar.YEAR);
//        int diffMonth = today.get(Calendar.MONTH) - fechaNac.get(Calendar.MONTH);
//        int diffDay = today.get(Calendar.DAY_OF_MONTH) - fechaNac.get(Calendar.DAY_OF_MONTH);
//
//        // Si está en ese año pero todavía no los ha cumplido
//        if (diffMonth < 0 || (diffMonth == 0 && diffDay < 0)) {
//            diffYear = diffYear - 1;
//        }
//
//        if(fechaNac.get(Calendar.DAY_OF_MONTH) >= today.get(Calendar.DAY_OF_MONTH)){
//            months = 12 - (fechaNac.get(Calendar.MONTH) - today.get(Calendar.MONTH));
//        }else{
//            months = 11 - (fechaNac.get(Calendar.MONTH) - today.get(Calendar.MONTH));
//        }
//
//        return diffYear + " años y " + months + " meses";
//    }

    public String calculeAge(Date birthDate) {
        int years = 0;
        int months = 0;
        int days = 0;

        //create calendar object for birth day
        Calendar birthDay = Calendar.getInstance();
        birthDay.setTimeInMillis(birthDate.getTime());

        //create calendar object for current day
        long currentTime = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTime);

        //Get difference between years
        years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
        int currMonth = now.get(Calendar.MONTH) + 1;
        int birthMonth = birthDay.get(Calendar.MONTH) + 1;

        //Get difference between months
        months = currMonth - birthMonth;

        //if month difference is in negative then reduce years by one
        //and calculate the number of months.
        if (months < 0) {
            years--;
            months = 12 - birthMonth + currMonth;
            if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE))
                months--;
        } else if (months == 0 && now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
            years--;
            months = 11;
        }

        //Calculate the days
        if (now.get(Calendar.DATE) > birthDay.get(Calendar.DATE))
            days = now.get(Calendar.DATE) - birthDay.get(Calendar.DATE);
        else if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
            int today = now.get(Calendar.DAY_OF_MONTH);
            now.add(Calendar.MONTH, -1);
            days = now.getActualMaximum(Calendar.DAY_OF_MONTH) - birthDay.get(Calendar.DAY_OF_MONTH) + today;
        } else {
            days = 0;
            if (months == 12) {
                years++;
                months = 0;
            }
        }

        String cadena = years + " años, " + months + " meses y " + days + " días";
        return cadena;
    }

    public String getPercentage(String n){
        String percentage;
        double number;

        try {
            if (!n.isEmpty()) {
                number = Double.parseDouble(n);
            }else{
                number = 0.0;
            }

            percentage = (int) (number * 100) + " %";

            return percentage;
        }catch (Exception ex){
            return "0.00";
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel2.setDescription("This is Channel 2");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }

    // saber si una actividad esta funcionando
    public Boolean isActivityRunning(Class activityClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                    return true;
            }
        }

        return false;
    }

    // mostrar una alerta sencilla (con ok)
    public void showAlert(String title, String message) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setTitle(title)
                .setCancelable(true)
                .setPositiveButton("Ok", (dialog, id) -> dialog.cancel())
                .show();
    }


    public boolean verifyServerConnection() {
        com.mds.ocapp.application.ConnectionClass connectionClass = new com.mds.ocapp.application.ConnectionClass(context);

        if (connectionClass.ConnectionMDS() != null) {
            return true;
        } else {
            return false;
        }
    }

    public PreparedStatement execute_SP(String lcNombreSP) {

        PreparedStatement loComando = null;
        com.mds.ocapp.application.ConnectionClass connectionClass = new com.mds.ocapp.application.ConnectionClass(context);
        String lcMensaje = "";

        try {
            Connection loConexion = connectionClass.ConnectionMDS();
            if (loConexion == null) {
                lcMensaje = "Error en la Conexión al Servidor SQL";
            } else {
                loComando = loConexion.prepareStatement(lcNombreSP);
            }
        } catch (Exception ex) {
            lcMensaje = "Error al Crear SP " + lcNombreSP;
        }

        return loComando;
    }


    // slide the view from below itself to the current position
    public void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }


    public void dialogCheckConnection() {
        final ProgressDialog progressDialog;

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Espera un momento por favor..."); // Setting Message
        progressDialog.setTitle("Revisando conexión al Servidor"); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        new Thread(() -> {
            Looper.prepare();
            try {
                com.mds.ocapp.application.ConnectionClass connectionClass = new com.mds.ocapp.application.ConnectionClass(getApplicationContext());

                if (connectionClass.ConnectionMDS() != null) {
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    showToast("No se pudo establecer conexión con el Servidor");
                }

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Ocurrió el error" + e);
            }
            progressDialog.dismiss();
        }).start();
    }

    public boolean checkConnection() {
        boolean connected = false;
        try {
            ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            // Recupera todas las redes (tanto móviles como wifi)
            NetworkInfo[] redes = connec.getAllNetworkInfo();

            for (int i = 0; i < redes.length; i++) {
                // Si alguna red tiene conexión, se devuelve true
                if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                    connected = true;
                }
            }
            return connected;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isOnline(Context context) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    ////This is used to show an Alert Box
    //public AlertDialog.Builder buildDialog(Context c) {
//
    //    AlertDialog.Builder builder = new AlertDialog.Builder(c);
    //    builder.setTitle("No Internet Connection");
    //    builder.setMessage("Seems like your Internet connection is down. Please Check");
//
    //    builder.setPositiveButton("OK", (dialog, which) -> {
    //        dialog.dismiss();
    //        finish();
    //    });
//
    //    return builder;
    //}

    public boolean verifyFileExist(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public String convertFileTo64(String path) {
        String stringBase64 = null;
        Bitmap bitmap;

        if (verifyFileExist(path)) {

            try {

                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                bitmap = BitmapFactory.decodeFile(path);
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
                bitmap = rotateBitmap(bitmap, orientation);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                stringBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            } catch (Exception ex) {
                showToast("Ocurrió un error al hacer la conversión");
                return null;
            }
        } else {
            showToast("Este archivo no existe");
            return null;
        }

        return stringBase64;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    /*public double round(double num) {
        try {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            return Double.parseDouble(df.format(num));
        }catch (Exception ex){
            showToast("Ocurrió un error al redondear, repórtalo: " + ex);
            return 0.0;
        }
    }*/

    public String formattedNumber(double value){
        String percentage, pattern = "###,###,###.###";

        try {

            DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
            formatSymbols.setDecimalSeparator('.');
            formatSymbols.setGroupingSeparator(',');
            DecimalFormat formatter = new DecimalFormat(DECIMAL_FORMAT, formatSymbols);
            return formatter.format(value);

            //DecimalFormat myFormatter = new DecimalFormat(pattern);
            //String output = myFormatter.format(value);
            ////System.out.println(value + "  " + pattern + "  " + output);
            //return output;

            //return String.valueOf(value);

        }catch(Exception ex){
            showLog("Ocurrió un error al formatear un número: " + ex);
            return String.valueOf(value);
        }
    }

    public String shortString(String text, int characters){
        String str = "";
        int cntDigits;
        cntDigits = text.length();

        if(cntDigits > characters){
            str = text.substring(0,characters-3) + "...";
        }else{
            str = text;

            for(int i = cntDigits; i < characters; i++){
                str = str+" ";
            }

            str = text;
        }

        return str;
    }

    public String getDifferenceTime(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : "+ endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        String strDifference = "";
        if(elapsedDays != 0) {
            strDifference += elapsedDays + " días";
        }else if(elapsedHours != 0) {
            strDifference += " " + elapsedHours + " horas";
        }else if(elapsedMinutes != 0) {
            strDifference += " " + elapsedMinutes + " minutos";
        }else if(elapsedSeconds != 0) {
            strDifference += " " + elapsedSeconds + " segundos";
        }

        return strDifference;
    }

    public void darkenStatusBar(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //activity.getWindow().setStatusBarColor(
                //       darkenColor(atrColor("colorStatusBar")));
                activity.getWindow().setStatusBarColor(color);
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    public boolean statusPermissionUbication() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showAlertPermissionUbication();
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean statusPermissionWriteExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ActivityCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                showAlertPermissionWriteExternalStorage();
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public void showAlertPermissionUbication(){
        final BaseApp functionsapp = new BaseApp(context);

        new AlertDialog.Builder(context)
                .setMessage("Es necesario el permiso de ubicación, ¿desea activarlo ahora?")
                .setCancelable(true)
                .setPositiveButton("Sí", (dialog, id) -> requestPermissionUbication())
                .setNegativeButton("No", (dialog, id) -> {
                    functionsapp.showToast("Debes aceptar el permiso de Ubicación para poder usar la App");
                    return;

                })
                .show();
    }

    public void showAlertPermissionWriteExternalStorage(){
        new AlertDialog.Builder(context)
                .setMessage("Es necesario el permiso de almacenamiento, ¿desea activarlo ahora?")
                .setCancelable(true)
                .setPositiveButton("Sí", (dialog, id) -> requestPermissionWriteExternalStorage())
                .setNegativeButton("No", (dialog, id) -> {
                    showToast("Debes aceptar el permiso de Almacenamiento para poder usar la App");
                    return;

                })
                .show();
    }

    public void requestPermissionUbication(){
        ActivityCompat.requestPermissions((Activity) context, new String[]{ACCESS_FINE_LOCATION}, 1);
    }


    public void requestPermissionWriteExternalStorage(){
        ActivityCompat.requestPermissions((Activity) context, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
    }
    // Code to darken the color supplied (mostly color of toolbar)
    public int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public void backupDBRealm(){

        try {
            realm = Realm.getDefaultInstance();

            //if(functionsapp.isSuperUser()) {

            if (statusPermissionWriteExternalStorage()) {
                barBackup = new ProgressDialog(context);
                barBackup.setTitle("Creando copia de seguridad");
                barBackup.setMessage("Este proceso puede tardar un par de segundos...");
                barBackup.setCancelable(false);
                barBackup.show();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {

                    try {
                        File exportRealmFile = null;
                        File exportRealmPATH;

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            exportRealmPATH = new File(Environment.getExternalStorageDirectory() +
                                    File.separator + DIRECTORY_BACKUPS);
                        }else{
                            exportRealmPATH = new File(context.getExternalFilesDir(null) +
                                    File.separator + DIRECTORY_BACKUPS);
                        }

                        if (!exportRealmPATH.exists()) {
                            exportRealmPATH.mkdirs();
                        }

                        String exportRealmFileName = "abpollo-data-" + getCurrentDateFlat() + ".realm";

                        Log.d("Realm", "Realm DB Path = " + realm.getPath());

                        // create a backup file
                        exportRealmFile = new File(exportRealmPATH, exportRealmFileName);

                        // if backup file already exists, delete it

                        if (exportRealmPATH.listFiles().length > 4) {
                            // here deleted last file
                            exportRealmFile.delete();

                            File[] files = exportRealmPATH.listFiles();
                            Arrays.sort(files);
                            File file = files[0];
                            file.delete();
                        }

                        File finalExportRealmFile = exportRealmFile;

                        realm.executeTransactionAsync(realm -> {
                            // copy current realm to backup file
                            realm.writeCopyTo(finalExportRealmFile);
                            // Do something on success
                        });

                        showToast("Se ha creado la Copia de Seguridad con éxito");
                    } catch (Exception ex) {
                        showAlertDialog("error", "Error", "Ocurrió un error al crear la copia de seguridad, reporta el siguiente error al Dpto de Sistemas: " + ex, true);
                    }

                    barBackup.dismiss();

                }, 1000);
                //}else{
                //    functionsapp.showToast("Función en Beta, aún no está disponible para todos los usuarios.");
                //}
            }
        }catch (Exception ex){
            showToast("Ocurrió un error inesperado, inténtalo de nuevo.");
        }
    }

    public void restoreDB(String fileName) {

        try {
            realm = Realm.getDefaultInstance();

            if(statusPermissionWriteExternalStorage()) {

                barBackup = new ProgressDialog(context);
                barBackup.setTitle("Restaurando base de datos...");
                barBackup.setMessage("Este proceso puede tardar un par de segundos...");
                barBackup.setCancelable(false);
                barBackup.show();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {

                    try {
                        //Restore
                    /*
                    String fileName = null;

                    File importRealmPATH = new File(Environment.getExternalStorageDirectory() +
                            File.separator + DIRECTORY_BACKUPS);
                    File[] files = importRealmPATH.listFiles();

                    if (files.length > 0) {
                        Arrays.sort(files);
                        for (int i = 0; i < files.length; i++) {
                            File file = files[i];

                            if (i == files.length - 1) {
                                fileName = file.getName();
                            }
                        }
                        */

                        String restoreFilePath;
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            restoreFilePath = Environment.getExternalStorageDirectory() +
                                    File.separator + DIRECTORY_BACKUPS + "/" + fileName;
                        }else{
                            restoreFilePath = context.getExternalFilesDir(null) +
                                    File.separator + DIRECTORY_BACKUPS + "/" + fileName;
                        }

                        copyBundledRealmFile(restoreFilePath);
                        Log.d("Realm", "Data restore is done");

                        showAlert("Base de datos recuperada", "Base de datos" + fileName + " restaurada, es necesario cerrar y volver a abrir la Aplicación.");
                        //} else {
                        //    showToast("No hay copia de seguridad que se pueda recuperar.");
                        //}
                    } catch (Exception ex) {
                        showAlert("Error", "Ocurrió un error al restaurar la última copia de seguridad.");
                    }

                    barBackup.dismiss();

                }, 1000);

            }else{
                showToast("No se puede recuperar una Base de Datos sin permisos de Archivos");
            }
        }catch (Exception ex){
            showAlertDialog("error", "Error", "No se pudo recuperar la Base de Datos, error: " + ex, true);
        }
    }

    private String copyBundledRealmFile(String oldFilePath) {
        try {
            realm = Realm.getDefaultInstance();

            File file = new File(dbPath());

            FileOutputStream outputStream = new FileOutputStream(file);
            FileInputStream inputStream = new FileInputStream(new File(oldFilePath));

            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return file.getAbsolutePath();
        } catch (io.realm.internal.IOException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String dbPath(){
        realm = Realm.getDefaultInstance();

        return realm.getPath();
    }

    public void vibrate(int ms){
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(ms);
            }
        }catch (Exception ex){
            showLog("Ocurrió un error");
        }
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }

        return sb.toString();
    }

    public int getNumberDay(){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        try{
            switch (day) {
                case Calendar.MONDAY:
                    // Current day is Sunday
                    return 2;
                case Calendar.TUESDAY:
                    // Current day is Monday
                    return 3;
                case Calendar.WEDNESDAY:
                    // etc.
                    return 4;
                case Calendar.THURSDAY:
                    return 5;
                case Calendar.FRIDAY:
                    return 6;
                case Calendar.SATURDAY:
                    return 7;
                case Calendar.SUNDAY:
                    return 1;
                default:
                    return 0;
            }
        }catch (Exception ex){
            showToast("Ocurrió un error: " + ex);
            return 0;
        }
    }

    public int getYearDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public int getMonthDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    public int getDayDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public float distanceBetweenCoordinates(double lat1, double long1, double lat2, double long2) {
        float[] distance = new float[1];
        Location.distanceBetween(lat1, long1, lat2, long2, distance);
        return distance[0];
    }

    public String getStringText(int string_id) {
        try {
            return context.getResources().getString(string_id);
        } catch (Exception ex) {
            showToast("Ocurrió un error");
            return "";
        }
    }

    public String numberToText(String number, boolean mayusculas) {
        String literal = "";
        String parte_decimal;
        //si el numero utiliza (.) en lugar de (,) -> se reemplaza
        number = number.replace(".", ",");
        //si el numero no tiene parte decimal, se le agrega ,00
        if(number.indexOf(",")==-1){
            number = number + ",00";
        }
        //se valida formato de entrada -> 0,00 y 999 999 999,00
        if (Pattern.matches("\\d{1,9},\\d{1,2}", number)) {
            //se divide el numero 0000000,00 -> entero y decimal
            String Num[] = number.split(",");
            //de da formato al numero decimal
            parte_decimal = Num[1] + "/100 M.N.";
            //se convierte el numero a literal
            if (Integer.parseInt(Num[0]) == 0) {//si el valor es cero
                literal = "cero ";
            } else if (Integer.parseInt(Num[0]) > 999999) {//si es millon
                literal = getMillones(Num[0]);
            } else if (Integer.parseInt(Num[0]) > 999) {//si es miles
                literal = getMiles(Num[0]);
            } else if (Integer.parseInt(Num[0]) > 99) {//si es centena
                literal = getCentenas(Num[0]);
            } else if (Integer.parseInt(Num[0]) > 9) {//si es decena
                literal = getDecenas(Num[0]);
            } else {//sino unidades -> 9
                literal = getUnidades(Num[0]);
            }
            //devuelve el resultado en mayusculas o minusculas
            if (mayusculas) {
                return (literal + parte_decimal).toUpperCase();
            } else {
                return (literal + parte_decimal);
            }
        } else {//error, no se puede convertir
            return literal = null;
        }
    }

    private String getUnidades(String numero) {// 1 - 9
        //si tuviera algun 0 antes se lo quita -> 09 = 9 o 009=9
        String num = numero.substring(numero.length() - 1);
        return UNIDADES[Integer.parseInt(num)];
    }

    private String getDecenas(String num) {// 99
        int n = Integer.parseInt(num);
        if (n < 10) {//para casos como -> 01 - 09
            return getUnidades(num);
        } else if (n > 19) {//para 20...99
            String u = getUnidades(num);
            if (u.equals("")) { //para 20,30,40,50,60,70,80,90
                return DECENAS[Integer.parseInt(num.substring(0, 1)) + 8];
            } else {
                return DECENAS[Integer.parseInt(num.substring(0, 1)) + 8] + "y " + u;
            }
        } else {//numeros entre 11 y 19
            return DECENAS[n - 10];
        }
    }

    private String getCentenas(String num) {// 999 o 099
        if( Integer.parseInt(num)>99 ){//es centena
            if (Integer.parseInt(num) == 100) {//caso especial
                return " cien ";
            } else {
                return CENTENAS[Integer.parseInt(num.substring(0, 1))] + getDecenas(num.substring(1));
            }
        }else{//por Ej. 099
            //se quita el 0 antes de convertir a decenas
            return getDecenas(Integer.parseInt(num)+"");
        }
    }

    private String getMiles(String numero) {// 999 999
        //obtiene las centenas
        String c = numero.substring(numero.length() - 3);
        //obtiene los miles
        String m = numero.substring(0, numero.length() - 3);
        String n="";
        //se comprueba que miles tenga valor entero
        if (Integer.parseInt(m) > 0) {
            n = getCentenas(m);
            return n + "mil " + getCentenas(c);
        } else {
            return "" + getCentenas(c);
        }

    }

    private String getMillones(String numero) { //000 000 000
        //se obtiene los miles
        String miles = numero.substring(numero.length() - 6);
        //se obtiene los millones
        String millon = numero.substring(0, numero.length() - 6);
        String n = "";
        if(millon.length()>1){
            n = getCentenas(millon) + "millones ";
        }else{
            n = getUnidades(millon) + "millon ";
        }
        return n + getMiles(miles);
    }

    public double round(double number){
        return (double)Math.round(number * 1000d) / 1000d;
    }

    public String conAcentos(String text){
        try{

            StringBuilder newText = new StringBuilder();

            for(int x = 0; x < text.length(); x++){
                String value = text.substring(x, x+1);

                switch (value){
                    case "A":
                    case "a":
                    case "Á":
                    case "á":
                        newText.append("[AaÁá]");
                    break;

                    case "E":
                    case "e":
                    case "É":
                    case "é":
                        newText.append("[EeÉé]");
                   break;

                    case "I":
                    case "i":
                    case "Í":
                    case "í":
                        newText.append("[IiÍí]");
                   break;

                    case "O":
                    case "o":
                    case "Ó":
                    case "ó":
                        newText.append("[OoÓó]");
                    break;

                    case "U":
                    case "u":
                    case "Ú":
                    case "ú":
                        newText.append("[UuÚú]");
                   break;
                }
            }

            return newText.toString();
        }catch (Exception ex){
            showToast("Ocurrió un error interno.");
            return "";
        }
    }

    public String charSiete(int nValor){
        try{
            return String.format("%7s", nValor);
        }catch (Exception ex){
            showToast("Ocurrió un error al convertir un valor a char.");
            return "";
        }
    }

    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "dd/MM/yyyy";
        String outputPattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static java.sql.Date convertDateToSQLDate(Date uDate) {
        java.sql.Date sDate = new java.sql.Date(uDate.getTime());
        return sDate;
    }
}

