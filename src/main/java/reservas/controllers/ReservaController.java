package reservas.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import reservas.models.ConflictResponse;
import reservas.models.ErrorResponse;
import reservas.models.Reserva;
import reservas.models.SuccessResponse;
import reservas.services.ReservaService;
import spark.Request;
import spark.Response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class ReservaController {
    
    private ReservaService reservaService;
    private Gson gson;
    
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
        // Configurar Gson con adaptadores para LocalDate y LocalTime
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .create();
    }
    
    /**
     * Obtiene todas las reservas o filtra por recurso
     * @param req request de Spark
     * @param res response de Spark
     * @return lista de reservas en JSON
     */
    public Object obtenerReservas(Request req, Response res) {
        String recurso = req.queryParams("recurso");
        
        List<Reserva> resultado;
        
        if (recurso != null && !recurso.isEmpty()) {
            resultado = reservaService.obtenerReservasPorRecurso(recurso);
        } else {
            resultado = reservaService.obtenerTodasLasReservas();
        }
        
        res.status(200);
        return resultado;
    }
    
    /**
     * Obtiene una reserva específica por ID
     * @param req request de Spark con parámetro :id
     * @param res response de Spark
     * @return reserva en JSON o error 404
     */
    public Object obtenerReservaPorId(Request req, Response res) {
        try {
            Long id = Long.parseLong(req.params(":id"));
            
            Optional<Reserva> reserva = reservaService.obtenerReservaPorId(id);
            
            if (reserva.isPresent()) {
                res.status(200);
                return reserva.get();
            } else {
                res.status(404);
                return new ErrorResponse("Reserva no encontrada con ID: " + id);
            }
        } catch (NumberFormatException e) {
            res.status(400);
            return new ErrorResponse("ID inválido: debe ser un número");
        }
    }
    
    /**
     * Crea una nueva reserva
     * @param req request de Spark con body JSON
     * @param res response de Spark
     * @return reserva creada en JSON o error
     */
    public Object crearReserva(Request req, Response res) {
        try {
            Reserva nuevaReserva = gson.fromJson(req.body(), Reserva.class);
            Reserva reservaCreada = reservaService.crearReserva(nuevaReserva);
            
            res.status(201);
            return reservaCreada;
            
        } catch (ReservaService.ConflictException e) {
            res.status(409);
            return new ConflictResponse(e.getMessage(), e.getDetalle());
        } catch (IllegalArgumentException e) {
            res.status(400);
            return new ErrorResponse(e.getMessage());
        } catch (DateTimeParseException e) {
            res.status(400);
            return new ErrorResponse("Formato de fecha u hora inválido. Use 'yyyy-MM-dd' para fechas y 'HH:mm' para horas");
        } catch (Exception e) {
            res.status(400);
            return new ErrorResponse("JSON inválido: " + e.getMessage());
        }
    }
    
    /**
     * Cancela una reserva por ID
     * @param req request de Spark con parámetro :id
     * @param res response de Spark
     * @return mensaje de confirmación o error 404
     */
    public Object cancelarReserva(Request req, Response res) {
        try {
            Long id = Long.parseLong(req.params(":id"));
            
            Reserva reservaCancelada = reservaService.cancelarReserva(id);
            
            res.status(200);
            return new SuccessResponse("Reserva cancelada correctamente", reservaCancelada);
            
        } catch (NumberFormatException e) {
            res.status(400);
            return new ErrorResponse("ID inválido: debe ser un número");
        } catch (IllegalArgumentException e) {
            res.status(404);
            return new ErrorResponse(e.getMessage());
        }
    }
}
