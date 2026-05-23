package com.spacework.service;

import com.spacework.dao.*;
import com.spacework.model.*;
import com.spacework.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvaluacionService {
    @Autowired
    private EvaluacionDAO evaluacionDAO;

    @Autowired
    private TokenEvaluacionDAO tokenEvaluacionDAO;

    @Autowired
    private ReservaDAO reservaDAO;

    @Autowired
    private EmailService emailService;

    public Evaluacion crearEvaluacion(String token, Double calificacion, String comentario) {
        TokenEvaluacion tokenEval = new TokenEvaluacion();
        tokenEval.setToken(token);
        
        if (!tokenEval.esValido()) {
            throw new ConflictException("Token expirado o ya utilizado");
        }

        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new BusinessException("Calificación debe estar entre 1 y 5");
        }

        // Crear evaluación
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setCalificacion(calificacion.intValue());
        evaluacion.setComentario(comentario);
        // TODO: Guardar evaluación en BD
        // evaluacionDAO.crear(evaluacion);

        // TODO: Marcar token como utilizado en BD
        // tokenEval.setUtilizado(1);
        // tokenEvaluacionDAO.actualizar(tokenEval);

        return evaluacion;
    }

    public Evaluacion obtenerEvaluacion(Long idEvaluacion) {
        Evaluacion evaluacion = new Evaluacion();
        // TODO: Implementar obtención de evaluación
        return evaluacion;
    }

    public TokenEvaluacion validarToken(String token) {
        TokenEvaluacion tokenEval = new TokenEvaluacion();
        tokenEval.setToken(token);

        if (!tokenEval.esValido()) {
            throw new ConflictException("Token expirado o ya utilizado");
        }

        return tokenEval;
    }
}
