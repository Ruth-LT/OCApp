package com.mds.ocapp.models;

import io.realm.RealmObject;

public class Detail extends RealmObject {

    private int clave_articulo;
    private String articulo;
    private String nombre_articulo;
    private double cantidad;
    private double precio;
    private double importe;

    private double cantidad_real;
    private double cantidad_cliente;
    private double precio_cliente;
    private double importe_margen;
    private double porcentaje_margen;

    public Detail() {
    }

    public Detail(int clave_articulo, String articulo, String nombre_articulo, double cantidad, double precio, double importe, double cantidad_real, double cantidad_cliente, double precio_cliente, double importe_margen, double porcentaje_margen) {
        this.clave_articulo = clave_articulo;
        this.articulo = articulo;
        this.nombre_articulo = nombre_articulo;
        this.cantidad = cantidad;
        this.precio = precio;
        this.importe = importe;
        this.cantidad_real = cantidad_real;
        this.cantidad_cliente = cantidad_cliente;
        this.precio_cliente = precio_cliente;
        this.importe_margen = importe_margen;
        this.porcentaje_margen = porcentaje_margen;
    }

    public double getCantidad_cliente() {
        return cantidad_cliente;
    }

    public void setCantidad_cliente(double cantidad_cliente) {
        this.cantidad_cliente = cantidad_cliente;
    }

    public double getCantidad_real() {
        return cantidad_real;
    }

    public void setCantidad_real(double cantidad_real) {
        this.cantidad_real = cantidad_real;
    }

    public double getPrecio_cliente() {
        return precio_cliente;
    }

    public void setPrecio_cliente(double precio_cliente) {
        this.precio_cliente = precio_cliente;
    }

    public double getImporte_margen() {
        return importe_margen;
    }

    public void setImporte_margen(double importe_margen) {
        this.importe_margen = importe_margen;
    }

    public double getPorcentaje_margen() {
        return porcentaje_margen;
    }

    public void setPorcentaje_margen(double porcentaje_margen) {
        this.porcentaje_margen = porcentaje_margen;
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
}
