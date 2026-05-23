package com.spacework.model;

import java.util.Date;

/**
 * Modelo para Token de Evaluación
 * Representa un token temporal que permite a un cliente evaluar una reserva
 * sin necesidad de autenticarse (acceso vía email)
 */
public class TokenEvaluacion {
    
    private int idToken;
    private int idPago;
    private String token;
    private String emailCliente;
    private Date fechaCreacion;
    private Date fechaExpiracion;
    private int utilizado; // 0 = Pendiente, 1 = Ya fue usado

    // Constructor por defecto
    public TokenEvaluacion() {
    }

    // Constructor completo
    public TokenEvaluacion(int idToken, int idPago, String token, String emailCliente,
                          Date fechaCreacion, Date fechaExpiracion, int utilizado) {
        this.idToken = idToken;
        this.idPago = idPago;
        this.token = token;
        this.emailCliente = emailCliente;
        this.fechaCreacion = fechaCreacion;
        this.fechaExpiracion = fechaExpiracion;
        this.utilizado = utilizado;
    }
    
    // Constructor simplificado para creación
    public TokenEvaluacion(int idPago, String token, String emailCliente, Date fechaExpiracion) {
        this.idPago = idPago;
        this.token = token;
        this.emailCliente = emailCliente;
        this.fechaCreacion = new Date();
        this.fechaExpiracion = fechaExpiracion;
        this.utilizado = 0;
    }

    // Getters y Setters
    public int getIdToken() {
        return idToken;
    }

    public void setIdToken(int idToken) {
        this.idToken = idToken;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(Date fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public int getUtilizado() {
        return utilizado;
    }

    public void setUtilizado(int utilizado) {
        this.utilizado = utilizado;
    }

    /**
     * Verifica si el token está disponible para usar
     * @return true si no ha sido utilizado
     */
    public boolean estaDisponible() {
        return utilizado == 0;
    }

    /**
     * Verifica si el token está expirado
     * @return true si la fecha de expiración ya pasó
     */
    public boolean estaExpirado() {
        java.util.Date ahora = new java.util.Date();
        return ahora.after(fechaExpiracion);
    }

    /**
     * Verifica si el token es válido (disponible y no expirado)
     * @return true si puede ser utilizado
     */
    public boolean esValido() {
        return estaDisponible() && !estaExpirado();
    }

    @Override
    public String toString() {
        return "TokenEvaluacion{" +
                "idToken=" + idToken +
                ", idPago=" + idPago +
                ", token='" + token + '\'' +
                ", emailCliente='" + emailCliente + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaExpiracion=" + fechaExpiracion +
                ", utilizado=" + utilizado +
                '}';
    }
}
