package com.spacework.model;

public class Espacio {

    private int    idEspacio;
    private String nombre;
    private String tipo;           // SALA_REUNION / OFICINA / COWORKING / AUDITORIO
    private int    capacidad;
    private String ubicacion;
    private double precioPorHora;
    private String estado;         // ACTIVO / INACTIVO
    private String urlImagen;      // Base64 o DataURL

    public Espacio() {}

    public Espacio(int idEspacio, String nombre, String tipo, int capacidad,
                   String ubicacion, double precioPorHora, String estado, String urlImagen) {
        this.idEspacio    = idEspacio;
        this.nombre       = nombre;
        this.tipo         = tipo;
        this.capacidad    = capacidad;
        this.ubicacion    = ubicacion;
        this.precioPorHora = precioPorHora;
        this.estado       = estado;
        this.urlImagen    = urlImagen;
    }

    public int    getIdEspacio()     { return idEspacio; }
    public String getNombre()        { return nombre; }
    public String getTipo()          { return tipo; }
    public int    getCapacidad()     { return capacidad; }
    public String getUbicacion()     { return ubicacion; }
    public double getPrecioPorHora() { return precioPorHora; }
    public String getEstado()        { return estado; }
    public String getUrlImagen()      { return urlImagen; }

    public void setIdEspacio(int id)           { this.idEspacio = id; }
    public void setNombre(String nombre)        { this.nombre = nombre; }
    public void setTipo(String tipo)            { this.tipo = tipo; }
    public void setCapacidad(int capacidad)     { this.capacidad = capacidad; }
    public void setUbicacion(String ubicacion)  { this.ubicacion = ubicacion; }
    public void setPrecioPorHora(double precio) { this.precioPorHora = precio; }
    public void setEstado(String estado)        { this.estado = estado; }
    public void setUrlImagen(String urlImagen)    { this.urlImagen = urlImagen; }

    @Override
    public String toString() {
        return nombre + " - " + tipo + " (Cap: " + capacidad + ")";
    }
}
