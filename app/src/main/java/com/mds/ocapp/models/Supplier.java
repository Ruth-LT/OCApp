package com.mds.ocapp.models;

import io.realm.RealmObject;

public class Supplier extends RealmObject {

    private int proveedor;
    private String nombre_proveedor;

    public Supplier() {
    }

    public Supplier(int proveedor, String nombre_proveedor) {
        this.proveedor = proveedor;
        this.nombre_proveedor = nombre_proveedor;
    }

    public int getProveedor() {
        return proveedor;
    }

    public void setProveedor(int proveedor) {
        this.proveedor = proveedor;
    }

    public String getNombre_proveedor() {
        return nombre_proveedor;
    }

    public void setNombre_proveedor(String nombre_proveedor) {
        this.nombre_proveedor = nombre_proveedor;
    }
}
