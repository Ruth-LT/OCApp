package com.mds.ocapp.models;

import io.realm.RealmObject;

public class BranchOffice extends RealmObject {

    private String sucursal;
    private String nombre_sucursal;

    public BranchOffice() {
    }

    public BranchOffice(String sucursal, String nombre_sucursal) {
        this.sucursal = sucursal;
        this.nombre_sucursal = nombre_sucursal;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public String getNombre_sucursal() {
        return nombre_sucursal;
    }

    public void setNombre_sucursal(String nombre_sucursal) {
        this.nombre_sucursal = nombre_sucursal;
    }


}

