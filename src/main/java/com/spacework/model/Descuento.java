package com.spacework.model;

import java.util.Date;

/**
 * Modelo de Descuento
 * Representa códigos promocionales y descuentos disponibles
 */
public class Descuento {
    
    private int idDescuento;
    private String codigo;
    private String descripcion;
    private double porcentaje;
    private double montoMinimo;
    private Date fechaInicio;
    private Date fechaFin;
    private int usosMaximos;
    private int usosActuales;
    private String estado;          // ACTIVO, INACTIVO

    // Constructor vacío
    public Descuento() {}

    // Constructor con parámetros
    public Descuento(int idDescuento, String codigo, String descripcion, double porcentaje,
                     double montoMinimo, Date fechaInicio, Date fechaFin, int usosMaximos,
                     int usosActuales, String estado) {
        this.idDescuento = idDescuento;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.porcentaje = porcentaje;
        this.montoMinimo = montoMinimo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.usosMaximos = usosMaximos;
        this.usosActuales = usosActuales;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdDescuento() {
        return idDescuento;
    }

    public void setIdDescuento(int idDescuento) {
        this.idDescuento = idDescuento;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public double getMontoMinimo() {
        return montoMinimo;
    }

    public void setMontoMinimo(double montoMinimo) {
        this.montoMinimo = montoMinimo;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getUsosMaximos() {
        return usosMaximos;
    }

    public void setUsosMaximos(int usosMaximos) {
        this.usosMaximos = usosMaximos;
    }

    public int getUsosActuales() {
        return usosActuales;
    }

    public void setUsosActuales(int usosActuales) {
        this.usosActuales = usosActuales;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Método auxiliar para verificar si el descuento está vigente
    public boolean esVigente() {
        Date ahora = new Date();
        return estado.equals("ACTIVO") && 
               ahora.after(fechaInicio) && 
               ahora.before(fechaFin) &&
               (usosMaximos == 0 || usosActuales < usosMaximos);
    }
    
    // Método para validar descuento con fecha y monto específicos
    public boolean esValido(Date fechaActual, double monto) {
        if (fechaActual == null || !"ACTIVO".equals(estado)) {
            return false;
        }
        if (fechaActual.before(fechaInicio) || fechaActual.after(fechaFin)) {
            return false;
        }
        if (monto < montoMinimo) {
            return false;
        }
        if (usosActuales >= usosMaximos) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Descuento{" +
                "idDescuento=" + idDescuento +
                ", codigo='" + codigo + '\'' +
                ", porcentaje=" + porcentaje +
                ", estado='" + estado + '\'' +
                ", usosActuales=" + usosActuales + "/" + usosMaximos +
                '}';
    }
}
