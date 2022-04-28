package com.mds.ocapp.models;

import io.realm.RealmObject;

public class Article extends RealmObject {

    private int clave_articulo;
    private String articulo;
    private String nombre_articulo;

    public Article() {
    }

    public Article(int clave_articulo, String articulo, String nombre_articulo) {
        this.clave_articulo = clave_articulo;
        this.articulo = articulo;
        this.nombre_articulo = nombre_articulo;
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
