package com.mds.ocapp.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.mds.ocapp.activities.AboutActivity;
import com.mds.ocapp.activities.AccountActivity;
import com.mds.ocapp.activities.AddArticleActivity;
import com.mds.ocapp.activities.ChangeConnectionActivity;
import com.mds.ocapp.activities.LoginActivity;
import com.mds.ocapp.activities.MainActivity;
import com.mds.ocapp.activities.RestoreDBActivity;
import com.mds.ocapp.activities.SearchOrderActivity;
import com.mds.ocapp.activities.SelectSupplierActivity;
import com.mds.ocapp.models.Article;
import com.mds.ocapp.models.BranchOffice;
import com.mds.ocapp.models.City;
import com.mds.ocapp.models.Supplier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import io.realm.Realm;

public class FunctionsApp extends Application {

    private Realm realm;
    private static Context context;

    int nextID;
    String messagesSync = "";

    public FunctionsApp(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // funcion para ir al LoginActivity
    public void goLoginActivity() {
        Intent iLoginActivity = new Intent(context, LoginActivity.class);
        context.startActivity(iLoginActivity);
        ((Activity) (context)).finish();
    }

    // funcion para ir al MainActivity
    public void goMainActivity() {
        Intent iMainActivity = new Intent(context, MainActivity.class);
        context.startActivity(iMainActivity);
        ((Activity) (context)).finish();
    }

    // funcion para ir al MainActivity
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public void goChangeConnection() {
        Intent iChangeConnectionActivity = new Intent(context, ChangeConnectionActivity.class);
        iChangeConnectionActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(iChangeConnectionActivity);
        ((Activity) (context)).overridePendingTransition(0, 0);
        ((Activity) (context)).finish();
    }

    // funcion para ir al AboutActivity
    public void goAboutActivity() {
        Intent iAboutActivity = new Intent(context, AboutActivity.class);
        iAboutActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(iAboutActivity);
        ((Activity) (context)).overridePendingTransition(0, 0);
        ((Activity) (context)).finish();
    }

    // funcion para ir al AboutActivity
    public void goRestoreDBActivity() {
        Intent iRestoreDBActivity = new Intent(context, RestoreDBActivity.class);
        iRestoreDBActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(iRestoreDBActivity);
        ((Activity) (context)).overridePendingTransition(0, 0);
        ((Activity) (context)).finish();
    }

    public void goAccountActivity() {
        Intent iAccountActivity = new Intent(context, AccountActivity.class);
        context.startActivity(iAccountActivity);
        //((Activity) (context)).finish();
    }

    public void goSelectSupplierActivity(String cTypeSupplier) {
        Intent iAccountActivity = new Intent(context, SelectSupplierActivity.class);
        iAccountActivity.putExtra("cTypeSupplier", cTypeSupplier);
        context.startActivity(iAccountActivity);
        //((Activity) (context)).finish();
    }

    public void goAddArticleActivity() {
        Intent iAddArticle = new Intent(context, AddArticleActivity.class);
        context.startActivity(iAddArticle);
    }

    public void goSearchOrderActivity() {
        Intent iSearchOrderActivity = new Intent(context, SearchOrderActivity.class);
        context.startActivity(iSearchOrderActivity);
    }

    public void downloadData(){
        Supplier supplier;
        Article article;
        Class classItem;
        City city;
        BranchOffice branchOffice;

        BaseApp baseApp = new BaseApp(context);
        realm = Realm.getDefaultInstance();

        try{
            boolean isResultSet;
            int countResults = 0, rowsCount = 0;

            baseApp.closeKeyboard();

            try {
                com.mds.ocapp.application.ConnectionClass connectionClass = new com.mds.ocapp.application.ConnectionClass(context);

                if (connectionClass.ConnectionMDS() != null) {
                    PreparedStatement loComando = baseApp.execute_SP("EXECUTE DiCampo.dbo.Descarga_Datos_OC_Android");

                    if (loComando == null) {
                        baseApp.showSnackBar("Error al Crear SP Descarga_Datos_OC_Android");
                    }else{
                        try {
                            isResultSet = loComando.execute();

                            realm.beginTransaction();
                            realm.delete(Supplier.class);
                            realm.delete(Article.class);
                            realm.delete(Class.class);
                            realm.delete(BranchOffice.class);
                            realm.commitTransaction();

                            while(true) {
                                if(isResultSet) {

                                    if(countResults == 0) {
                                        ResultSet Datos = loComando.getResultSet();
                                        while (Datos.next()) {
                                            realm.beginTransaction();
                                            supplier = new Supplier(
                                                    Datos.getInt("proveedor"),
                                                    Datos.getString("nombre_proveedor").trim()
                                            );

                                            realm.copyToRealm(supplier);
                                            realm.commitTransaction();
                                        }

                                        Datos.close();
                                    }

                                    if(countResults == 1) {
                                        ResultSet Datos1 = loComando.getResultSet();
                                        while (Datos1.next()) {
                                            realm.beginTransaction();
                                            article = new Article(
                                                    Datos1.getInt("clave_articulo"),
                                                    Datos1.getString("articulo").trim(),
                                                    Datos1.getString("nombre_articulo").trim()
                                            );

                                            realm.copyToRealm(article);
                                            realm.commitTransaction();
                                        }

                                        Datos1.close();
                                    }

                                    if(countResults == 2) {
                                        ResultSet Datos2 = loComando.getResultSet();
                                        while (Datos2.next()) {
                                            realm.beginTransaction();
                                            classItem = new Class(
                                                    Datos2.getInt("clase_proveedor"),
                                                    Datos2.getString("nombre_clase").trim()
                                            );

                                            realm.copyToRealm(classItem);
                                            realm.commitTransaction();
                                        }

                                        Datos2.close();
                                    }

                                    if (countResults == 3) {
                                        ResultSet Datos3 = loComando.getResultSet();
                                        while (Datos3.next()) {
                                            realm.beginTransaction();
                                            city = new City(
                                                    Datos3.getInt("ciudad"),
                                                    Datos3.getString("nombre")
                                            );

                                            realm.copyToRealm(city);
                                            realm.commitTransaction();
                                        }

                                        Datos3.close();
                                    }

                                    if (countResults == 4) {
                                        ResultSet Datos4 = loComando.getResultSet();
                                        while (Datos4.next()) {
                                            realm.beginTransaction();
                                            branchOffice = new BranchOffice(
                                                    Datos4.getString("sucursal"),
                                                    Datos4.getString("nombre_sucursal")
                                            );

                                            realm.copyToRealm(branchOffice);
                                            realm.commitTransaction();
                                        }

                                        Datos4.close();
                                    }
                                } else {
                                    if(loComando.getUpdateCount() == -1) {
                                        break;
                                    }

                                    baseApp.showLog( "Result {} is just a count: {}" + countResults  + ", " + loComando.getUpdateCount());
                                }

                                countResults ++;
                                isResultSet = loComando.getMoreResults();
                            }

                        } catch (Exception ex) {
                            baseApp.showToast("Error al descargar los proveedores");
                            ex.printStackTrace();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                baseApp.showToast("Ocurrió el error" + e);
            }
        }catch (Exception ex){
            baseApp.showToast("Ocurrió el error: "  + ex);
        }
    }
}

