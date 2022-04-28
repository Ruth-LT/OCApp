package com.mds.ocapp.models;

import io.realm.RealmObject;

public class Order extends RealmObject {

    private int orden;
    private String fecha;
    private String sucursal;
    private String agricultor;
    private String estado_actual;

    public Order() {
    }

    public Order(int orden, String fecha, String sucursal, String agricultor, String estado_actual) {
        this.orden = orden;
        this.fecha = fecha;
        this.sucursal = sucursal;
        this.agricultor = agricultor;
        this.estado_actual = estado_actual;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public String getAgricultor() {
        return agricultor;
    }

    public void setAgricultor(String agricultor) {
        this.agricultor = agricultor;
    }

    public String getEstado_actual() {
        return estado_actual;
    }

    public void setEstado_actual(String estado_actual) {
        this.estado_actual = estado_actual;
    }
}

