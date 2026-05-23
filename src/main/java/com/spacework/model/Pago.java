package com.spacework.model;

import java.util.Date;

/**
 * Modelo de Pago
 * Representa un pago asociado a una reserva
 */
public class Pago {
    
    private int idPago;
    private int idReserva;
    private double monto;
    private String metodoPago;      // TARJETA, TRANSFERENCIA, EFECTIVO
    private String estadoPago;      // PENDIENTE, COMPLETADO, RECHAZADO, REEMBOLSADO
    private Date fechaPago;
    private Date fechaCreacion;
    private int idDescuento;           // 0 = sin descuento
    private double descuentoAplicado;  // monto descontado en S/.
    private double igv;                // Impuesto 18%
    private double montoFinal;         // Monto total con IGV
    private String referencia;         // Referencia de transacción

    // Constructor vacío
    public Pago() {}

    // Constructor con parámetros
    public Pago(int idPago, int idReserva, double monto, String metodoPago, 
                String estadoPago, Date fechaPago, Date fechaCreacion) {
        this.idPago = idPago;
        this.idReserva = idReserva;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.estadoPago = estadoPago;
        this.fechaPago = fechaPago;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdDescuento() { return idDescuento; }
    public void setIdDescuento(int idDescuento) { this.idDescuento = idDescuento; }
    public double getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(double descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }
    public double getIgv() { return igv; }
    public void setIgv(double igv) { this.igv = igv; }
    public double getMontoFinal() { return montoFinal; }
    public void setMontoFinal(double montoFinal) { this.montoFinal = montoFinal; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String toString() {
        return "Pago{" +
                "idPago=" + idPago +
                ", idReserva=" + idReserva +
                ", monto=" + monto +
                ", metodoPago='" + metodoPago + '\'' +
                ", estadoPago='" + estadoPago + '\'' +
                ", fechaPago=" + fechaPago +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
