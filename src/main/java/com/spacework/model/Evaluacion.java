package com.spacework.model;

import java.util.Date;

public class Evaluacion {
    
    private int idEvaluacion;
    private int idReserva;
    private int idCliente;
    private int calificacion;       // 1-5 (Nivel de Atención)
    private String comentario;
    private Date fechaEvaluacion;

    // Constructor vacío
    public Evaluacion() {}

    // Constructor con parámetros
    public Evaluacion(int idEvaluacion, int idReserva, int idCliente, 
                      int calificacion, String comentario, Date fechaEvaluacion) {
        this.idEvaluacion = idEvaluacion;
        this.idReserva = idReserva;
        this.idCliente = idCliente;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fechaEvaluacion = fechaEvaluacion;
    }

    // Getters y Setters
    public int getIdEvaluacion() {
        return idEvaluacion;
    }

    public void setIdEvaluacion(int idEvaluacion) {
        this.idEvaluacion = idEvaluacion;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        if (calificacion >= 1 && calificacion <= 5) {
            this.calificacion = calificacion;
        }
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Date getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(Date fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    // Método auxiliar para obtener representación en estrellas
    public String getEstrellas() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < calificacion; i++) {
            sb.append("★");
        }
        for (int i = calificacion; i < 5; i++) {
            sb.append("☆");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Evaluacion{" +
                "idEvaluacion=" + idEvaluacion +
                ", idReserva=" + idReserva +
                ", idCliente=" + idCliente +
                ", calificacion=" + calificacion + " " + getEstrellas() +
                ", fechaEvaluacion=" + fechaEvaluacion +
                '}';
    }
}
