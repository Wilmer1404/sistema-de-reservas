package com.spacework.model;

import java.io.Serializable;
import java.util.Date;

public class Auditoria implements Serializable {
    private Long idAuditoria;
    private Long idUsuario;
    private String tablaModificada;
    private String operacion;
    private String datosAntiguos;
    private String datosNuevos;
    private Date fechaOperacion;
    private String ipOrigen;

    public Auditoria() {}

    public Auditoria(String tablaModificada, String operacion, String datosAntiguos, String datosNuevos) {
        this.tablaModificada = tablaModificada;
        this.operacion = operacion;
        this.datosAntiguos = datosAntiguos;
        this.datosNuevos = datosNuevos;
    }

    public Long getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(Long idAuditoria) { this.idAuditoria = idAuditoria; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getTablaModificada() { return tablaModificada; }
    public void setTablaModificada(String tablaModificada) { this.tablaModificada = tablaModificada; }

    public String getOperacion() { return operacion; }
    public void setOperacion(String operacion) { this.operacion = operacion; }

    public String getDatosAntiguos() { return datosAntiguos; }
    public void setDatosAntiguos(String datosAntiguos) { this.datosAntiguos = datosAntiguos; }

    public String getDatosNuevos() { return datosNuevos; }
    public void setDatosNuevos(String datosNuevos) { this.datosNuevos = datosNuevos; }

    public Date getFechaOperacion() { return fechaOperacion; }
    public void setFechaOperacion(Date fechaOperacion) { this.fechaOperacion = fechaOperacion; }

    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }

    @Override
    public String toString() {
        return "Auditoria{" +
                "idAuditoria=" + idAuditoria +
                ", tablaModificada='" + tablaModificada + '\'' +
                ", operacion='" + operacion + '\'' +
                '}';
    }
}
