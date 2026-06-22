package com.construplan.empleado.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.construplan.empleado.model.entity.Empleado;
import com.construplan.empleado.model.entity.RegistroDiario;
import com.construplan.empleado.service.EmpleadoService;
import com.construplan.empleado.service.RegistroDiarioService;

@RestController
@RequestMapping("/empleado")
public class RegistroAsistenciaController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private RegistroDiarioService registroDiarioService;

    @PostMapping("/registro-asistencia")
    public ResponseEntity<Map<String, Object>> registrarAsistencia(
            Authentication authentication,
            @RequestParam("accion") String action) {

        Map<String, Object> response = new HashMap<>();

        // Retorno temprano en caso de que la autenticación haya expirado
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Sesión expirada");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Empleado empleado = empleadoService.buscarPorUsername(authentication.getName());
            int idEmpleado = empleado.getIdEmpleado();

            return procesarAccion(idEmpleado, action, response);

        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al procesar la solicitud");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Procesa las solicitudes según la acción indicada, delegando en el servicio correspondiente
    private ResponseEntity<Map<String, Object>> procesarAccion(
            int idEmpleado, 
            String action, 
            Map<String, Object> response) {

        switch (action) {
            case "entrada":
                RegistroDiario entrada = registroDiarioService.registrarEntrada(idEmpleado);
                response.put("success", true);
                response.put("message", "Entrada registrada exitosamente");
                response.put("hora", entrada.getHoraInicio().toString());
                break;

            case "salida":
                RegistroDiario salida = registroDiarioService.registrarSalida(idEmpleado);
                response.put("success", true);
                response.put("message", "Salida registrada exitosamente");
                response.put("hora", salida.getHoraFin().toString());
                break;

            case "completar-tarea":
                RegistroDiario tarea = registroDiarioService.completarTarea(idEmpleado);
                response.put("success", true);
                response.put("message", "Tarea completada exitosamente");
                response.put("hora", tarea.getAsignacion().getHoraMetaCompletada().toString());
                break;

            default:
                response.put("success", false);
                response.put("message", "Acción no válida");
                return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
