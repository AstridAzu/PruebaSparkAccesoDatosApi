package biblioteca.services;

import biblioteca.models.Libro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BibliotecaService {
    
    private Map<String, Libro> biblioteca;
    
    public BibliotecaService() {
        this.biblioteca = new HashMap<>();
        inicializarDatosEjemplo();
    }
    
    /**
     * Inicializa la biblioteca con datos de ejemplo
     */
    private void inicializarDatosEjemplo() {
        biblioteca.put("978-0134685991", new Libro("978-0134685991", "Effective Java", "Joshua Bloch", 2018));
        biblioteca.put("978-0596009205", new Libro("978-0596009205", "Head First Java", "Kathy Sierra", 2005));
        biblioteca.put("978-0132350884", new Libro("978-0132350884", "Clean Code", "Robert Martin", 2008));
    }
    
    /**
     * Obtiene todos los libros de la biblioteca
     * @return lista de todos los libros
     */
    public List<Libro> obtenerTodosLosLibros() {
        return biblioteca.values().stream().collect(Collectors.toList());
    }
    
    /**
     * Obtiene libros filtrados por autor
     * @param autor nombre del autor (búsqueda case-insensitive)
     * @return lista de libros del autor
     */
    public List<Libro> obtenerLibrosPorAutor(String autor) {
        return biblioteca.values().stream()
                .filter(libro -> libro.getAutor().toLowerCase().contains(autor.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene un libro por su ISBN
     * @param isbn el ISBN del libro
     * @return Optional con el libro si existe
     */
    public Optional<Libro> obtenerLibroPorIsbn(String isbn) {
        return Optional.ofNullable(biblioteca.get(isbn));
    }
    
    /**
     * Busca libros por título
     * @param query texto a buscar en el título (búsqueda parcial case-insensitive)
     * @return lista de libros que coinciden
     */
    public List<Libro> buscarLibrosPorTitulo(String query) {
        return biblioteca.values().stream()
                .filter(libro -> libro.getTitulo().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Crea un nuevo libro en la biblioteca
     * @param libro el libro a crear
     * @return el libro creado
     * @throws IllegalArgumentException si el libro ya existe o los datos son inválidos
     */
    public Libro crearLibro(Libro libro) {
        validarLibro(libro);
        
        if (biblioteca.containsKey(libro.getIsbn())) {
            throw new IllegalArgumentException("Ya existe un libro con ese ISBN");
        }
        
        if (!validarISBN(libro.getIsbn())) {
            throw new IllegalArgumentException("Formato de ISBN inválido");
        }
        
        biblioteca.put(libro.getIsbn(), libro);
        return libro;
    }
    
    /**
     * Actualiza un libro existente
     * @param isbn el ISBN del libro a actualizar
     * @param libroActualizado los nuevos datos del libro
     * @return el libro actualizado
     * @throws IllegalArgumentException si el libro no existe o los datos son inválidos
     */
    public Libro actualizarLibro(String isbn, Libro libroActualizado) {
        if (!biblioteca.containsKey(isbn)) {
            throw new IllegalArgumentException("Libro no encontrado con ISBN: " + isbn);
        }
        
        validarLibroActualizacion(libroActualizado);
        
        // Mantener el ISBN original
        libroActualizado.setIsbn(isbn);
        
        biblioteca.put(isbn, libroActualizado);
        return libroActualizado;
    }
    
    /**
     * Elimina un libro de la biblioteca
     * @param isbn el ISBN del libro a eliminar
     * @return el libro eliminado
     * @throws IllegalArgumentException si el libro no existe
     */
    public Libro eliminarLibro(String isbn) {
        Libro libroEliminado = biblioteca.remove(isbn);
        
        if (libroEliminado == null) {
            throw new IllegalArgumentException("Libro no encontrado con ISBN: " + isbn);
        }
        
        return libroEliminado;
    }
    
    /**
     * Valida que un libro tenga todos los campos requeridos
     * @param libro el libro a validar
     * @throws IllegalArgumentException si faltan campos requeridos
     */
    private void validarLibro(Libro libro) {
        if (libro.getIsbn() == null || libro.getIsbn().isEmpty()) {
            throw new IllegalArgumentException("ISBN es requerido");
        }
        
        if (libro.getTitulo() == null || libro.getTitulo().isEmpty()) {
            throw new IllegalArgumentException("Título es requerido");
        }
        
        if (libro.getAutor() == null || libro.getAutor().isEmpty()) {
            throw new IllegalArgumentException("Autor es requerido");
        }
    }
    
    /**
     * Valida que un libro tenga los campos requeridos para actualización
     * @param libro el libro a validar
     * @throws IllegalArgumentException si faltan campos requeridos
     */
    private void validarLibroActualizacion(Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().isEmpty()) {
            throw new IllegalArgumentException("Título es requerido");
        }
        
        if (libro.getAutor() == null || libro.getAutor().isEmpty()) {
            throw new IllegalArgumentException("Autor es requerido");
        }
    }
    
    /**
     * Valida el formato básico de un ISBN
     * @param isbn el ISBN a validar
     * @return true si el formato es válido
     */
    private boolean validarISBN(String isbn) {
        if (isbn == null) return false;
        
        // Eliminar guiones y espacios para validación
        String isbnLimpio = isbn.replaceAll("[- ]", "");
        
        // Validar que sea ISBN-10 o ISBN-13
        return isbnLimpio.length() == 10 || isbnLimpio.length() == 13;
    }
}
