package reservas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import reservas.controllers.LocalDateAdapter;
import reservas.controllers.LocalTimeAdapter;
import reservas.controllers.ReservaController;
import reservas.models.ErrorResponse;
import reservas.services.ReservaService;
import spark.ResponseTransformer;

import java.time.LocalDate;
import java.time.LocalTime;

import static spark.Spark.*;

public class ReservaAPI {

    private static ReservaService reservaService = new ReservaService();
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .create();
    private static ReservaController reservaController = new ReservaController(reservaService);

    public static void main(String[] args) {
        port(4567);
        configurarRutas();

        System.out.println("API de Reservas iniciada en http://localhost:4567");
        System.out.println("\nEndpoints disponibles:");
        System.out.println("  GET    /reservas           - Obtener todas las reservas");
        System.out.println("  GET    /reservas?recurso=X - Filtrar por recurso");
        System.out.println("  GET    /reservas/:id       - Obtener una reserva");
        System.out.println("  POST   /reservas           - Crear nueva reserva");
        System.out.println("  DELETE /reservas/:id       - Cancelar reserva");
    }

    /**
     * Registra todas las rutas de la API
     */
    public static void configurarRutas() {
        // Configurar ResponseTransformer para todas las respuestas JSON
        ResponseTransformer jsonTransformer = gson::toJson;

        // Configurar headers por defecto
        before((req, res) -> res.type("application/json"));

        // Rutas principales con ResponseTransformer usando el controlador
        get("/reservas", reservaController::obtenerReservas, jsonTransformer);
        get("/reservas/:id", reservaController::obtenerReservaPorId, jsonTransformer);
        post("/reservas", reservaController::crearReserva, jsonTransformer);
        delete("/reservas/:id", reservaController::cancelarReserva, jsonTransformer);

        // Manejo de rutas no encontradas
        notFound((req, res) -> {
            res.type("application/json");
            return gson.toJson(new ErrorResponse("Ruta no encontrada"));
        });

        // Manejo de errores internos
        internalServerError((req, res) -> {
            res.type("application/json");
            return gson.toJson(new ErrorResponse("Error interno del servidor"));
        });
    }
}
