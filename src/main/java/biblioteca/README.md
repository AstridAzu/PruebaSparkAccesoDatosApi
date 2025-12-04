# API de Biblioteca

Sistema REST API para gestión de biblioteca desarrollado con Spark Java Framework.

## 📍 Ubicación de Archivos

```
src/main/java/biblioteca/
├── BibliotecaAPI.java          # Punto de entrada y configuración de rutas
├── models/
│   ├── Libro.java              # Entidad principal de libro
│   ├── ErrorResponse.java      # Respuesta de error estándar
│   └── SuccessResponse.java    # Respuesta exitosa estándar
├── services/
│   └── BibliotecaService.java  # Lógica de negocio y validaciones
└── controllers/
    └── BibliotecaController.java # Handlers de peticiones HTTP
```

## 🏗️ Arquitectura

### Patrón MVC (Model-View-Controller)

#### **Modelos** (`models/`)
- `Libro.java`: Representa un libro con ISBN, título, autor y año
- `ErrorResponse.java`: Estructura para respuestas de error
- `SuccessResponse.java`: Estructura para respuestas exitosas con datos

#### **Servicios** (`services/`)
- `BibliotecaService.java`: 
  - Gestión de datos en memoria (HashMap)
  - Validaciones de negocio (ISBN, campos requeridos)
  - Operaciones CRUD completas
  - Búsqueda y filtrado de libros

#### **Controladores** (`controllers/`)
- `BibliotecaController.java`:
  - Parseo de requests HTTP
  - Invocación de servicios
  - Manejo de códigos de estado HTTP
  - Serialización de respuestas a JSON

#### **API Principal** (raíz)
- `BibliotecaAPI.java`:
  - Configuración del servidor Spark (puerto 4567)
  - Registro de rutas HTTP
  - Configuración de transformadores JSON
  - Manejo global de errores (404, 500)

## 📡 Endpoints Disponibles

### Obtener todos los libros
```
GET /libros
GET /libros?autor=NombreAutor
```

### Buscar libros por título
```
GET /libros/buscar?q=titulo
```

### Obtener un libro específico
```
GET /libros/:isbn
```

### Crear nuevo libro
```
POST /libros
Body: {
  "isbn": "978-0134685991",
  "titulo": "Effective Java",
  "autor": "Joshua Bloch",
  "anio": 2018
}
```

### Actualizar libro
```
PUT /libros/:isbn
Body: {
  "titulo": "Effective Java 3rd Edition",
  "autor": "Joshua Bloch",
  "anio": 2018
}
```

### Eliminar libro
```
DELETE /libros/:isbn
```

## 🔍 Validaciones Implementadas

1. **ISBN requerido y formato válido**: ISBN-10 o ISBN-13
2. **Título y autor requeridos**: No pueden estar vacíos
3. **ISBN único**: No permite duplicados
4. **Validación de JSON**: Manejo de errores de parseo

## 📦 Códigos de Estado HTTP

- `200 OK`: Operación exitosa
- `201 Created`: Libro creado
- `400 Bad Request`: Datos inválidos
- `404 Not Found`: Libro no encontrado
- `409 Conflict`: ISBN duplicado
- `500 Internal Server Error`: Error del servidor

## 🚀 Ejecución

```bash
# Compilar y ejecutar
java biblioteca.BibliotecaAPI

# La API estará disponible en:
http://localhost:4567
```

## 📝 Ejemplo de Uso

```bash
# Obtener todos los libros
curl http://localhost:4567/libros

# Crear un nuevo libro
curl -X POST http://localhost:4567/libros \
  -H "Content-Type: application/json" \
  -d '{"isbn":"978-0134685991","titulo":"Effective Java","autor":"Joshua Bloch","anio":2018}'

# Buscar libros
curl http://localhost:4567/libros/buscar?q=Java

# Filtrar por autor
curl http://localhost:4567/libros?autor=Bloch
```

## 🛠️ Tecnologías

- **Spark Java**: Framework web ligero
- **Gson**: Serialización/deserialización JSON
- **Java 8+**: Streams, Optional, Lambdas
