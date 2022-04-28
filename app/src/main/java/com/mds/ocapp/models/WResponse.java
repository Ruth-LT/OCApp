package com.mds.ocapp.models;

public class WResponse {
    private String ip_local;
    private String ip_externa;
    private String last_version;
    private String pin;


    public String getIp_local() {
        return ip_local;
    }

    public void setIp_local(String ip_local) {
        this.ip_local = ip_local;
    }

    public String getIp_externa() {
        return ip_externa;
    }

    public void setIp_externa(String ip_externa) {
        this.ip_externa = ip_externa;
    }

    public String getLast_version() {
        return last_version;
    }

    public void setLast_version(String last_version) {
        this.last_version = last_version;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}