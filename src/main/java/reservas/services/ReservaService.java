package reservas.services;

import reservas.models.EstadoReserva;
import reservas.models.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ReservaService {
    
    private Map<Long, Reserva> reservas;
    private AtomicLong idGenerator;
    
    public ReservaService() {
        this.reservas = new HashMap<>();
        this.idGenerator = new AtomicLong(1);
    }
    
    /**
     * Obtiene todas las reservas confirmadas
     * @return lista de reservas
     */
    public List<Reserva> obtenerTodasLasReservas() {
        return reservas.values().stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene reservas filtradas por recurso
     * @param recurso nombre del recurso
     * @return lista de reservas del recurso
     */
    public List<Reserva> obtenerReservasPorRecurso(String recurso) {
        return reservas.values().stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .filter(r -> r.getRecurso().equalsIgnoreCase(recurso))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene una reserva por su ID
     * @param id el ID de la reserva
     * @return Optional con la reserva si existe
     */
    public Optional<Reserva> obtenerReservaPorId(Long id) {
        Reserva reserva = reservas.get(id);
        if (reserva != null && reserva.getEstado() == EstadoReserva.CONFIRMADA) {
            return Optional.of(reserva);
        }
        return Optional.empty();
    }
    
    /**
     * Crea una nueva reserva validando conflictos y fechas
     * @param reserva la reserva a crear
     * @return la reserva creada con ID asignado
     * @throws IllegalArgumentException si hay conflictos o validaciones fallidas
     */
    public Reserva crearReserva(Reserva reserva) {
        // Validar campos requeridos
        validarCamposRequeridos(reserva);
        
        // Validar que la fecha sea futura o presente
        validarFechaFutura(reserva.getFecha());
        
        // Validar que la hora de fin sea posterior a la hora de inicio
        validarRangoHorario(reserva.getHoraInicio(), reserva.getHoraFin());
        
        // Detectar conflictos de horario
        Optional<Reserva> conflicto = detectarConflicto(reserva);
        if (conflicto.isPresent()) {
            Reserva reservaConflictiva = conflicto.get();
            throw new ConflictException(
                "Conflicto de horario",
                String.format("La sala ya está reservada de %s a %s",
                    reservaConflictiva.getHoraInicio(),
                    reservaConflictiva.getHoraFin())
            );
        }
        
        // Asignar ID y estado
        Long nuevoId = idGenerator.getAndIncrement();
        reserva.setId(nuevoId);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        
        // Guardar reserva
        reservas.put(nuevoId, reserva);
        
        return reserva;
    }
    
    /**
     * Cancela una reserva
     * @param id el ID de la reserva a cancelar
     * @return la reserva cancelada
     * @throws IllegalArgumentException si la reserva no existe
     */
    public Reserva cancelarReserva(Long id) {
        Reserva reserva = reservas.get(id);
        
        if (reserva == null || reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalArgumentException("Reserva no encontrada con ID: " + id);
        }
        
        reserva.setEstado(EstadoReserva.CANCELADA);
        return reserva;
    }
    
    /**
     * Valida que todos los campos requeridos estén presentes
     * @param reserva la reserva a validar
     * @throws IllegalArgumentException si faltan campos
     */
    private void validarCamposRequeridos(Reserva reserva) {
        if (reserva.getRecurso() == null || reserva.getRecurso().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'recurso' es requerido");
        }
        
        if (reserva.getFecha() == null) {
            throw new IllegalArgumentException("El campo 'fecha' es requerido");
        }
        
        if (reserva.getHoraInicio() == null) {
            throw new IllegalArgumentException("El campo 'horaInicio' es requerido");
        }
        
        if (reserva.getHoraFin() == null) {
            throw new IllegalArgumentException("El campo 'horaFin' es requerido");
        }
        
        if (reserva.getNombreUsuario() == null || reserva.getNombreUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'nombreUsuario' es requerido");
        }
    }
    
    /**
     * Valida que la fecha sea presente o futura
     * @param fecha la fecha a validar
     * @throws IllegalArgumentException si la fecha es pasada
     */
    private void validarFechaFutura(LocalDate fecha) {
        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy)) {
            throw new IllegalArgumentException(
                "La fecha de reserva debe ser presente o futura. Fecha recibida: " + fecha
            );
        }
    }
    
    /**
     * Valida que la hora de fin sea posterior a la hora de inicio
     * @param horaInicio hora de inicio
     * @param horaFin hora de fin
     * @throws IllegalArgumentException si el rango es inválido
     */
    private void validarRangoHorario(LocalTime horaInicio, LocalTime horaFin) {
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException(
                String.format("La hora de fin (%s) debe ser posterior a la hora de inicio (%s)",
                    horaFin, horaInicio)
            );
        }
    }
    
    /**
     * Detecta si hay conflicto de horario con reservas existentes
     * @param nuevaReserva la reserva a verificar
     * @return Optional con la reserva conflictiva si existe
     */
    private Optional<Reserva> detectarConflicto(Reserva nuevaReserva) {
        return reservas.values().stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .filter(r -> nuevaReserva.seSolapaCon(r))
                .findFirst();
    }
    
    /**
     * Excepción personalizada para conflictos de reserva
     */
    public static class ConflictException extends IllegalArgumentException {
        private String detalle;
        
        public ConflictException(String mensaje, String detalle) {
            super(mensaje);
            this.detalle = detalle;
        }
        
        public String getDetalle() {
            return detalle;
        }
    }
}
