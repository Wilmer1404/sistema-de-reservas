package com.spacework.service;

import com.spacework.dao.DescuentoDAO;
import com.spacework.model.Descuento;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DescuentoService {

    private final DescuentoDAO descuentoDAO = new DescuentoDAO();

    public List<Descuento> listar() throws Exception {
        return descuentoDAO.listar();
    }

    public Descuento buscarPorId(int id) throws Exception {
        return descuentoDAO.buscarPorId(id);
    }

    public void insertar(Descuento d) throws Exception {
        d.setCodigo(d.getCodigo().toUpperCase().trim());
        d.setUsosActuales(0);
        d.setEstado("ACTIVO");
        descuentoDAO.insertar(d);
    }

    public void actualizar(int id, Descuento datos) throws Exception {
        Descuento d = descuentoDAO.buscarPorId(id);
        if (d == null) throw new IllegalArgumentException("Descuento no encontrado");
        if (datos.getCodigo() != null) d.setCodigo(datos.getCodigo().toUpperCase().trim());
        if (datos.getDescripcion() != null) d.setDescripcion(datos.getDescripcion());
        if (datos.getPorcentaje() > 0) d.setPorcentaje(datos.getPorcentaje());
        if (datos.getMontoMinimo() >= 0) d.setMontoMinimo(datos.getMontoMinimo());
        if (datos.getUsosMaximos() > 0) d.setUsosMaximos(datos.getUsosMaximos());
        if (datos.getEstado() != null) d.setEstado(datos.getEstado());
        if (datos.getFechaInicio() != null) d.setFechaInicio(datos.getFechaInicio());
        if (datos.getFechaFin() != null) d.setFechaFin(datos.getFechaFin());
        descuentoDAO.actualizar(d);
    }

    public void desactivar(int id) throws Exception {
        descuentoDAO.desactivar(id);
    }

    public Descuento validar(String codigo, double monto) throws Exception {
        Descuento d = descuentoDAO.buscarPorCodigo(codigo.toUpperCase().trim());
        if (d == null) throw new IllegalArgumentException("Código de descuento no encontrado");
        boolean valido = descuentoDAO.validarCodigo(codigo.toUpperCase().trim(), monto);
        if (!valido) throw new IllegalStateException(
            "Descuento inválido, vencido o monto mínimo no cumplido (S/. " + d.getMontoMinimo() + ")");
        return d;
    }
}
