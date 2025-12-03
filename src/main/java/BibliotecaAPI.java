import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static spark.Spark.*;


public class BibliotecaAPI {

    private static Map<String, Libro> biblioteca = new HashMap<>();
    private static Gson gson = new Gson();
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$");

    public static void main(String[] args) {
        port(4567);
        configurarRutas();

        // Datos de ejemplo
        biblioteca.put("978-0134685991", new Libro("978-0134685991", "Effective Java", "Joshua Bloch", 2018));
        biblioteca.put("978-0596009205", new Libro("978-0596009205", "Head First Java", "Kathy Sierra", 2005));
        biblioteca.put("978-0132350884", new Libro("978-0132350884", "Clean Code", "Robert Martin", 2008));

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

        // Rutas principales con ResponseTransformer
        get("/libros", BibliotecaAPI::obtenerLibros, jsonTransformer);
        get("/libros/buscar", BibliotecaAPI::buscarLibros, jsonTransformer);
        get("/libros/:isbn", BibliotecaAPI::obtenerLibroPorIsbn, jsonTransformer);
        post("/libros", BibliotecaAPI::crearLibro, jsonTransformer);
        put("/libros/:isbn", BibliotecaAPI::actualizarLibro, jsonTransformer);
        delete("/libros/:isbn", BibliotecaAPI::eliminarLibro, jsonTransformer);

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

    /**
     * Obtiene todos los libros o filtra por autor
     * @param req request de Spark
     * @param res response de Spark
     * @return lista de libros en JSON
     */
    public static Object obtenerLibros(Request req, Response res) {
        String autor = req.queryParams("autor");

        List<Libro> resultado;

        if (autor != null && !autor.isEmpty()) {
            // Filtrar por autor (búsqueda case-insensitive)
            resultado = biblioteca.values().stream()
                    .filter(libro -> libro.getAutor().toLowerCase().contains(autor.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            resultado = biblioteca.values().stream().collect(Collectors.toList());
        }

        res.status(200);
        return resultado;
    }

    /**
     * Obtiene un libro específico por ISBN
     * @param req request de Spark con parámetro :isbn
     * @param res response de Spark
     * @return libro en JSON o error 404
     */
    public static Object obtenerLibroPorIsbn(Request req, Response res) {
        String isbn = req.params(":isbn");

        Libro libro = biblioteca.get(isbn);

        if (libro != null) {
            res.status(200);
            return libro;
        } else {
            res.status(404);
            return new ErrorResponse("Libro no encontrado con ISBN: " + isbn);
        }
    }

    /**
     * Crea un nuevo libro
     * @param req request de Spark con body JSON
     * @param res response de Spark
     * @return libro creado en JSON
     */
    public static Object crearLibro(Request req, Response res) {
        try {
            Libro nuevoLibro = gson.fromJson(req.body(), Libro.class);

            // Validar campos requeridos
            if (nuevoLibro.getIsbn() == null || nuevoLibro.getIsbn().isEmpty()) {
                res.status(400);
                return new ErrorResponse("ISBN es requerido");
            }

            if (nuevoLibro.getTitulo() == null || nuevoLibro.getTitulo().isEmpty()) {
                res.status(400);
                return new ErrorResponse("Título es requerido");
            }

            if (nuevoLibro.getAutor() == null || nuevoLibro.getAutor().isEmpty()) {
                res.status(400);
                return new ErrorResponse("Autor es requerido");
            }

            // Validar formato de ISBN básico
            if (!validarISBN(nuevoLibro.getIsbn())) {
                res.status(400);
                return new ErrorResponse("Formato de ISBN inválido");
            }

            // Verificar que no exista un libro con el mismo ISBN
            if (biblioteca.containsKey(nuevoLibro.getIsbn())) {
                res.status(409);
                return new ErrorResponse("Ya existe un libro con ese ISBN");
            }

            biblioteca.put(nuevoLibro.getIsbn(), nuevoLibro);
            res.status(201);
            return nuevoLibro;

        } catch (Exception e) {
            res.status(400);
            return new ErrorResponse("JSON inválido: " + e.getMessage());
        }
    }

    /**
     * Actualiza un libro existente
     * @param req request de Spark con parámetro :isbn y body JSON
     * @param res response de Spark
     * @return libro actualizado en JSON o error 404
     */
    public static Object actualizarLibro(Request req, Response res) {
        String isbn = req.params(":isbn");

        if (!biblioteca.containsKey(isbn)) {
            res.status(404);
            return new ErrorResponse("Libro no encontrado con ISBN: " + isbn);
        }

        try {
            Libro libroActualizado = gson.fromJson(req.body(), Libro.class);

            // Validar campos requeridos
            if (libroActualizado.getTitulo() == null || libroActualizado.getTitulo().isEmpty()) {
                res.status(400);
                return new ErrorResponse("Título es requerido");
            }

            if (libroActualizado.getAutor() == null || libroActualizado.getAutor().isEmpty()) {
                res.status(400);
                return new ErrorResponse("Autor es requerido");
            }

            // Mantener el ISBN original
            libroActualizado.setIsbn(isbn);

            biblioteca.put(isbn, libroActualizado);
            res.status(200);
            return libroActualizado;

        } catch (Exception e) {
            res.status(400);
            return new ErrorResponse("JSON inválido: " + e.getMessage());
        }
    }

    /**
     * Elimina un libro por ISBN
     * @param req request de Spark con parámetro :isbn
     * @param res response de Spark
     * @return mensaje de confirmación o error 404
     */
    public static Object eliminarLibro(Request req, Response res) {
        String isbn = req.params(":isbn");

        Libro libroEliminado = biblioteca.remove(isbn);

        if (libroEliminado != null) {
            res.status(200);
            return new SuccessResponse("Libro eliminado correctamente", libroEliminado);
        } else {
            res.status(404);
            return new ErrorResponse("Libro no encontrado con ISBN: " + isbn);
        }
    }

    /**
     * Busca libros por título (búsqueda parcial)
     * @param req request de Spark con query param ?q=
     * @param res response de Spark
     * @return lista de libros que coinciden
     */
    public static Object buscarLibros(Request req, Response res) {
        String query = req.queryParams("q");

        if (query == null || query.isEmpty()) {
            res.status(400);
            return new ErrorResponse("Parámetro 'q' es requerido para la búsqueda");
        }

        List<Libro> resultados = biblioteca.values().stream()
                .filter(libro -> libro.getTitulo().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        res.status(200);
        return resultados;
    }

    /**
     * Valida el formato básico de un ISBN
     * @param isbn el ISBN a validar
     * @return true si el formato es válido
     */
    private static boolean validarISBN(String isbn) {
        if (isbn == null) return false;

        // Eliminar guiones y espacios para validación
        String isbnLimpio = isbn.replaceAll("[- ]", "");

        // Validar que sea ISBN-10 o ISBN-13
        return isbnLimpio.length() == 10 || isbnLimpio.length() == 13;
    }
}

// Clase Libro
class Libro {
    private String isbn;
    private String titulo;
    private String autor;
    private int anio;

    public Libro() {}

    public Libro(String isbn, String titulo, String autor, int anio) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.anio = anio;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
}

// Clase para respuestas de error
class ErrorResponse {
    private String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}

// Clase para respuestas exitosas
class SuccessResponse {
    private String mensaje;
    private Object data;

    public SuccessResponse(String mensaje, Object data) {
        this.mensaje = mensaje;
        this.data = data;
    }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}