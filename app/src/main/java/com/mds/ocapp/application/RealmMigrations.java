package com.mds.ocapp.application;

import android.content.Context;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class RealmMigrations implements RealmMigration {

    private static Context context;
    private Realm realm;
    private RealmConfiguration realmConfi;

    public RealmMigrations(Context context) {
        this.realm = Realm.getDefaultInstance();
        this.context = context;
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        com.mds.ocapp.application.FunctionsApp functionsapp = new com.mds.ocapp.application.FunctionsApp(context);
        BaseApp baseApp = new BaseApp(context);

        final RealmSchema schema = realm.getSchema();

        try {

            if (oldVersion == 1) {
                schema.create("Personal")
                        .addField("persona", int.class)
                        .addField("nombre_persona", String.class);

                oldVersion++;
            }

        }catch(Exception ex){
            baseApp.showAlert("Error", "Ocurrió un error, repórtalo al dpto de Sistemas: " + ex);
        }
    }

    public int hashCode() {
        return 3;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof RealmMigrations);
    }
}