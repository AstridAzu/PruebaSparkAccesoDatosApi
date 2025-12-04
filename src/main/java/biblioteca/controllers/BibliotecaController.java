package biblioteca.controllers;

import com.google.gson.Gson;
import biblioteca.models.ErrorResponse;
import biblioteca.models.Libro;
import biblioteca.models.SuccessResponse;
import biblioteca.services.BibliotecaService;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Optional;

public class BibliotecaController {
    
    private BibliotecaService bibliotecaService;
    private Gson gson;
    
    public BibliotecaController(BibliotecaService bibliotecaService, Gson gson) {
        this.bibliotecaService = bibliotecaService;
        this.gson = gson;
    }
    
    /**
     * Obtiene todos los libros o filtra por autor
     * @param req request de Spark
     * @param res response de Spark
     * @return lista de libros en JSON
     */
    public Object obtenerLibros(Request req, Response res) {
        String autor = req.queryParams("autor");
        
        List<Libro> resultado;
        
        if (autor != null && !autor.isEmpty()) {
            resultado = bibliotecaService.obtenerLibrosPorAutor(autor);
        } else {
            resultado = bibliotecaService.obtenerTodosLosLibros();
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
    public Object obtenerLibroPorIsbn(Request req, Response res) {
        String isbn = req.params(":isbn");
        
        Optional<Libro> libro = bibliotecaService.obtenerLibroPorIsbn(isbn);
        
        if (libro.isPresent()) {
            res.status(200);
            return libro.get();
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
    public Object crearLibro(Request req, Response res) {
        try {
            Libro nuevoLibro = gson.fromJson(req.body(), Libro.class);
            Libro libroCreado = bibliotecaService.crearLibro(nuevoLibro);
            
            res.status(201);
            return libroCreado;
            
        } catch (IllegalArgumentException e) {
            res.status(e.getMessage().contains("Ya existe") ? 409 : 400);
            return new ErrorResponse(e.getMessage());
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
    public Object actualizarLibro(Request req, Response res) {
        String isbn = req.params(":isbn");
        
        try {
            Libro libroActualizado = gson.fromJson(req.body(), Libro.class);
            Libro resultado = bibliotecaService.actualizarLibro(isbn, libroActualizado);
            
            res.status(200);
            return resultado;
            
        } catch (IllegalArgumentException e) {
            res.status(e.getMessage().contains("no encontrado") ? 404 : 400);
            return new ErrorResponse(e.getMessage());
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
    public Object eliminarLibro(Request req, Response res) {
        String isbn = req.params(":isbn");
        
        try {
            Libro libroEliminado = bibliotecaService.eliminarLibro(isbn);
            
            res.status(200);
            return new SuccessResponse("Libro eliminado correctamente", libroEliminado);
            
        } catch (IllegalArgumentException e) {
            res.status(404);
            return new ErrorResponse(e.getMessage());
        }
    }
    
    /**
     * Busca libros por título (búsqueda parcial)
     * @param req request de Spark con query param ?q=
     * @param res response de Spark
     * @return lista de libros que coinciden
     */
    public Object buscarLibros(Request req, Response res) {
        String query = req.queryParams("q");
        
        if (query == null || query.isEmpty()) {
            res.status(400);
            return new ErrorResponse("Parámetro 'q' es requerido para la búsqueda");
        }
        
        List<Libro> resultados = bibliotecaService.buscarLibrosPorTitulo(query);
        
        res.status(200);
        return resultados;
    }
}
