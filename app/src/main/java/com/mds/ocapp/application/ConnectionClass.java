package com.mds.ocapp.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {
    private static Context context;

    public ConnectionClass(Context context) {
        this.context = context;
    }

    String classs = "net.sourceforge.jtds.jdbc.Driver";
    String ip, db, un, password;

    @SuppressLint("NewApi")
    public Connection ConnectionMDS() {
        com.mds.ocapp.application.SPClass spClass = new com.mds.ocapp.application.SPClass(context);

        ip =  spClass.strGetSP("IPConnection");
        db = spClass.strGetSP("DBConnection");
        un = spClass.strGetSP("UserConnection");
        password = "O4TQ9IX6RC2U";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy
                .Builder()
                .permitAll()
                .build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;

        try {
            Class.forName(classs).newInstance();

            ConnURL = "jdbc:jtds:sqlserver://" + ip +
                    ";databaseName=" + db +
                    ";user=" + un +
                    ";password=" + password + ";";

            conn = DriverManager.getConnection(ConnURL);
            System.out.println("Connected to the database!!!");

        } catch (SQLException se) {
            Log.e("ERRO SQL", se.getMessage());

        } catch (ClassNotFoundException e) {
            Log.e("ERRO CLASS", e.getMessage());

        } catch (Exception e) {
            Log.e("ERRO EXCEPTION", e.getMessage());
        }
        return conn;
    }
}