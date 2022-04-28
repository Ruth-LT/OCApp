package com.mds.ocapp.models;

import io.realm.RealmObject;

public class Class extends RealmObject {

    private int clase_proveedor;
    private String nombre_clase;

    public Class() {
    }

    public Class(int clase_proveedor, String nombre_clase) {
        this.clase_proveedor = clase_proveedor;
        this.nombre_clase = nombre_clase;
    }

    public int getClase_proveedor() {
        return clase_proveedor;
    }

    public void setClase_proveedor(int clase_proveedor) {
        this.clase_proveedor = clase_proveedor;
    }

    public String getNombre_clase() {
        return nombre_clase;
    }

    public void setNombre_clase(String nombre_clase) {
        this.nombre_clase = nombre_clase;
    }
}
