package com.mds.ocapp.models;

public class Files {

    private String nombre_archivo;
    private String tamano;
    private String fecha;

    public Files(
            String nombre_archivo,
            String tamano,
            String fecha) {

        this.nombre_archivo = nombre_archivo;
        this.tamano = tamano;
        this.fecha = fecha;
    }

    public String getNombre_archivo() {
        return nombre_archivo;
    }

    public void setNombre_archivo(String nombre_archivo) {
        this.nombre_archivo = nombre_archivo;
    }

    public String getTamano() {
        return tamano;
    }

    public void setTamano(String tamano) {
        this.tamano = tamano;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
