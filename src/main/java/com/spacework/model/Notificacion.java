package com.spacework.model;

import java.util.Date;

public class Notificacion {
    
    private int idNotificacion;
    private int idUsuario;
    private String tipo;            // RESERVA, PAGO, RECORDATORIO, PROMOCION, SISTEMA
    private String asunto;
    private String mensaje;
    private boolean leida;
    private Date fechaCreacion;
    private Date fechaLeida;

    // Constructor vacío
    public Notificacion() {}

    // Constructor con parámetros
    public Notificacion(int idNotificacion, int idUsuario, String tipo, String asunto,
                        String mensaje, boolean leida, Date fechaCreacion, Date fechaLeida) {
        this.idNotificacion = idNotificacion;
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.leida = leida;
        this.fechaCreacion = fechaCreacion;
        this.fechaLeida = fechaLeida;
    }

    // Getters y Setters
    public int getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaLeida() {
        return fechaLeida;
    }

    public void setFechaLeida(Date fechaLeida) {
        this.fechaLeida = fechaLeida;
    }

    // Método auxiliar para obtener icono según tipo
    public String getIconoTipo() {
        switch (tipo) {
            case "RESERVA":
                return "📅";
            case "PAGO":
                return "💳";
            case "RECORDATORIO":
                return "⏰";
            case "PROMOCION":
                return "🎁";
            case "SISTEMA":
                return "⚙️";
            default:
                return "📬";
        }
    }

    @Override
    public String toString() {
        return "Notificacion{" +
                "idNotificacion=" + idNotificacion +
                ", idUsuario=" + idUsuario +
                ", tipo='" + tipo + '\'' +
                ", asunto='" + asunto + '\'' +
                ", leida=" + leida +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
