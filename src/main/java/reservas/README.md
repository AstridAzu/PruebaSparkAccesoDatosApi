# API de Reservas

Sistema REST API para gestión de reservas con validación de fechas y detección de conflictos de horario, desarrollado con Spark Java Framework.

## 📍 Ubicación de Archivos

```
src/main/java/reservas/
├── ReservaAPI.java                  # Punto de entrada y configuración de rutas
├── models/
│   ├── Reserva.java                 # Entidad principal de reserva
│   ├── EstadoReserva.java           # Enum de estados (CONFIRMADA, CANCELADA)
│   ├── ConflictResponse.java        # Respuesta para conflictos HTTP 409
│   ├── ErrorResponse.java           # Respuesta de error estándar
│   └── SuccessResponse.java         # Respuesta exitosa estándar
├── services/
│   └── ReservaService.java          # Lógica de negocio, validaciones y detección de conflictos
└── controllers/
    ├── ReservaController.java       # Handlers de peticiones HTTP
    ├── LocalDateAdapter.java        # Adaptador Gson para LocalDate
    └── LocalTimeAdapter.java        # Adaptador Gson para LocalTime
```

## 🏗️ Arquitectura

### Patrón MVC (Model-View-Controller)

#### **Modelos** (`models/`)
- `Reserva.java`: Representa una reserva con ID, recurso, fecha, horarios y usuario
  - Método `seSolapaCon()`: Detecta solapamiento de horarios entre reservas
- `EstadoReserva.java`: Enum con estados CONFIRMADA y CANCELADA
- `ConflictResponse.java`: Respuesta específica para conflictos de horario (409)
- `ErrorResponse.java` y `SuccessResponse.java`: Respuestas estándar

#### **Servicios** (`services/`)
- `ReservaService.java`:
  - Gestión de reservas en memoria con IDs autogenerados
  - **Validaciones**:
    - Campos requeridos
    - Fechas futuras o presentes
    - Rangos horarios válidos (horaFin > horaInicio)
    - Detección de conflictos de horario
  - **Excepción personalizada**: `ConflictException` para conflictos HTTP 409

#### **Controladores** (`controllers/`)
- `ReservaController.java`:
  - Parseo de requests HTTP
  - Manejo de excepciones específicas (ConflictException)
  - Asignación de códigos de estado apropiados
- `LocalDateAdapter.java` y `LocalTimeAdapter.java`:
  - Serialización/deserialización de LocalDate (yyyy-MM-dd) y LocalTime (HH:mm)

#### **API Principal** (raíz)
- `ReservaAPI.java`:
  - Configuración del servidor Spark (puerto 4567)
  - Configuración de Gson con adaptadores para fechas/horas
  - Registro de rutas HTTP
  - Manejo global de errores

## 📡 Endpoints Disponibles

### Obtener todas las reservas
```
GET /reservas
GET /reservas?recurso=SalaDeReunionesA
```

### Obtener una reserva específica
```
GET /reservas/:id
```

### Crear nueva reserva
```
POST /reservas
Body: {
  "recurso": "Sala de Reuniones A",
  "fecha": "2025-12-01",
  "horaInicio": "10:00",
  "horaFin": "12:00",
  "nombreUsuario": "María López"
}
```

**Respuesta exitosa (201):**
```json
{
  "id": 1,
  "recurso": "Sala de Reuniones A",
  "fecha": "2025-12-01",
  "horaInicio": "10:00",
  "horaFin": "12:00",
  "nombreUsuario": "María López",
  "estado": "CONFIRMADA"
}
```

**Respuesta de conflicto (409):**
```json
{
  "error": "Conflicto de horario",
  "detalle": "La sala ya está reservada de 10:00 a 12:00"
}
```

### Cancelar reserva
```
DELETE /reservas/:id
```

## 🔍 Validaciones Implementadas

### 1. Validación de Campos Requeridos
- `recurso`, `fecha`, `horaInicio`, `horaFin`, `nombreUsuario` no pueden estar vacíos

### 2. Validación de Fechas
- La fecha debe ser **presente o futura**
- No se permiten reservas en fechas pasadas

### 3. Validación de Horarios
- `horaFin` debe ser posterior a `horaInicio`
- Formato: HH:mm (ejemplo: 10:00, 14:30)

### 4. Detección de Conflictos
- Verifica solapamiento de horarios en el mismo recurso y fecha
- Algoritmo: `horaInicio < otra.horaFin AND horaFin > otra.horaInicio`
- Solo considera reservas con estado CONFIRMADA

## 📦 Códigos de Estado HTTP

- `200 OK`: Operación exitosa
- `201 Created`: Reserva creada exitosamente
- `400 Bad Request`: Datos inválidos o formato incorrecto
- `404 Not Found`: Reserva no encontrada
- `409 Conflict`: Conflicto de horario detectado
- `500 Internal Server Error`: Error del servidor

## 🚀 Ejecución

```bash
# Compilar y ejecutar
java reservas.ReservaAPI

# La API estará disponible en:
http://localhost:4567
```

## 📝 Ejemplos de Uso

### Crear una reserva
```bash
curl -X POST http://localhost:4567/reservas \
  -H "Content-Type: application/json" \
  -d '{
    "recurso": "Sala de Reuniones A",
    "fecha": "2025-12-15",
    "horaInicio": "10:00",
    "horaFin": "12:00",
    "nombreUsuario": "María López"
  }'
```

### Intentar reservar con conflicto
```bash
curl -X POST http://localhost:4567/reservas \
  -H "Content-Type: application/json" \
  -d '{
    "recurso": "Sala de Reuniones A",
    "fecha": "2025-12-15",
    "horaInicio": "11:00",
    "horaFin": "13:00",
    "nombreUsuario": "Pedro Ruiz"
  }'
```
**Respuesta:** HTTP 409 Conflict

### Filtrar por recurso
```bash
curl http://localhost:4567/reservas?recurso=Sala%20de%20Reuniones%20A
```

### Cancelar una reserva
```bash
curl -X DELETE http://localhost:4567/reservas/1
```

## 🎯 Características Especiales

### Detección Inteligente de Solapamiento
El sistema detecta conflictos incluso en casos parciales:
- Reserva nueva comienza durante una existente
- Reserva nueva termina durante una existente
- Reserva nueva contiene completamente a una existente
- Reserva existente contiene completamente a la nueva

### Formatos de Fecha y Hora
- **Fecha**: `yyyy-MM-dd` (ISO 8601)
- **Hora**: `HH:mm` (formato 24 horas)

### Estados de Reserva
- **CONFIRMADA**: Reserva activa y considerada en validaciones
- **CANCELADA**: Reserva eliminada lógicamente, no se considera en conflictos

## 🛠️ Tecnologías

- **Spark Java**: Framework web ligero
- **Gson**: Serialización/deserialización JSON con adaptadores personalizados
- **Java 8+**: LocalDate, LocalTime, Streams, Optional, Lambdas
- **AtomicLong**: Generación thread-safe de IDs únicos
