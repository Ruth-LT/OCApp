package com.mds.ocapp.models;

import io.realm.RealmObject;

public class Detail extends RealmObject {

    private int clave_articulo;
    private String articulo;
    private String nombre_articulo;
    private double cantidad;
    private double precio;
    private double importe;

    public Detail() {
    }

    public Detail(int clave_articulo, String articulo, String nombre_articulo, double cantidad, double precio, double importe) {
        this.clave_articulo = clave_articulo;
        this.articulo = articulo;
        this.nombre_articulo = nombre_articulo;
        this.cantidad = cantidad;
        this.precio = precio;
        this.importe = importe;
    }

    public int getClave_articulo() {
        return clave_articulo;
    }

    public void setClave_articulo(int clave_articulo) {
        this.clave_articulo = clave_articulo;
    }

    public String getArticulo() {
        return articulo;
    }

    public void setArticulo(String articulo) {
        this.articulo = articulo;
    }

    public String getNombre_articulo() {
        return nombre_articulo;
    }

    public void setNombre_articulo(String nombre_articulo) {
        this.nombre_articulo = nombre_articulo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getImporte() {
        return importe;
    }

    public void setImporte(double importe) {
        this.importe = importe;
    }
}
