package biblioteca;

import com.google.gson.Gson;
import biblioteca.controllers.BibliotecaController;
import biblioteca.models.ErrorResponse;
import biblioteca.services.BibliotecaService;
import spark.ResponseTransformer;

import static spark.Spark.*;


public class BibliotecaAPI {

    private static Gson gson = new Gson();
    private static BibliotecaService bibliotecaService = new BibliotecaService();
    private static BibliotecaController bibliotecaController = new BibliotecaController(bibliotecaService, gson);

    public static void main(String[] args) {
        port(4567);
        configurarRutas();

        System.out.println("API de Biblioteca iniciada en http://localhost:4567");
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
        get("/libros", bibliotecaController::obtenerLibros, jsonTransformer);
        get("/libros/buscar", bibliotecaController::buscarLibros, jsonTransformer);
        get("/libros/:isbn", bibliotecaController::obtenerLibroPorIsbn, jsonTransformer);
        post("/libros", bibliotecaController::crearLibro, jsonTransformer);
        put("/libros/:isbn", bibliotecaController::actualizarLibro, jsonTransformer);
        delete("/libros/:isbn", bibliotecaController::eliminarLibro, jsonTransformer);

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
