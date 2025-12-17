package com.example.f4sINV.model;

public class Warehouse {
    private String codigo;
    private String descripcion;

    public Warehouse(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
