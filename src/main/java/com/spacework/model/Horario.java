package com.spacework.model;

public class Horario {
    
    private int idHorario;
    private int idEspacio;
    private int diaSemana;          // 0=Domingo, 1=Lunes, ..., 6=Sábado
    private String horaApertura;    // Formato HH:MM
    private String horaCierre;      // Formato HH:MM
    private String estado;          // ACTIVO, INACTIVO

    // Constructor vacío
    public Horario() {}

    // Constructor con parámetros
    public Horario(int idHorario, int idEspacio, int diaSemana, 
                   String horaApertura, String horaCierre, String estado) {
        this.idHorario = idHorario;
        this.idEspacio = idEspacio;
        this.diaSemana = diaSemana;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(int idHorario) {
        this.idHorario = idHorario;
    }

    public int getIdEspacio() {
        return idEspacio;
    }

    public void setIdEspacio(int idEspacio) {
        this.idEspacio = idEspacio;
    }

    public int getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(int diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(String horaApertura) {
        this.horaApertura = horaApertura;
    }

    public String getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(String horaCierre) {
        this.horaCierre = horaCierre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Método auxiliar para obtener nombre del día
    public String getNombreDia() {
        String[] dias = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
        return diaSemana >= 0 && diaSemana <= 6 ? dias[diaSemana] : "Desconocido";
    }

    @Override
    public String toString() {
        return "Horario{" +
                "idHorario=" + idHorario +
                ", idEspacio=" + idEspacio +
                ", diaSemana=" + diaSemana + " (" + getNombreDia() + ")" +
                ", horaApertura='" + horaApertura + '\'' +
                ", horaCierre='" + horaCierre + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
