package com.mds.ocapp.application;

import android.content.Context;
import android.content.SharedPreferences;
/*
Class SPClass By Kevin Solís Chacón
***** To Create Class Object DatesSP *****
 // activitie
        DatesSP spClass = new DatesSP(this);
 // fragment
        DatesSP spClass = new DatesSP(getActivity());
***** To Delete a value *****
        spClass.deleteSP("username");
***** To Save a value *****
        spClass.strSetSP("username","Lolita");
***** To Retreive a value *****
        spClass.strGetSP("username");
*/

public class SPClass {

    private static Context context;
    public final static String PREFS_NAME = "ventas_abpollo";

    public SPClass(Context context){
        this.context = context;
    }

    public static void deleteSP(String key){
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public static void strSetSP(String key, String value){
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String strGetSP(String key) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME,0);
        return preferences.getString(key,"ND");
    }

    public static void intSetSP(String key, int value){
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int intGetSP(String key) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME,0);
        return preferences.getInt(key, 0);
    }

    public static void boolSetSP(String key, boolean value){
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean boolGetSP(String key) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME,0);
        return preferences.getBoolean(key,false);
    }
}