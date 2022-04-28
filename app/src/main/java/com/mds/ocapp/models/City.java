package com.mds.ocapp.models;

import io.realm.RealmObject;

public class City extends RealmObject {

    private int ciudad;
    private String nombre;

    public City() {
    }

    public City(int ciudad, String nombre) {
        this.ciudad = ciudad;
        this.nombre = nombre;
    }

    public int getCiudad() {
        return ciudad;
    }

    public void setCiudad(int ciudad) {
        this.ciudad = ciudad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
