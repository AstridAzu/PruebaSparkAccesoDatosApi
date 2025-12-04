package reservas.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reserva {
    private Long id;
    private String recurso;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String nombreUsuario;
    private EstadoReserva estado;

    public Reserva() {
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public Reserva(Long id, String recurso, LocalDate fecha, LocalTime horaInicio, 
                   LocalTime horaFin, String nombreUsuario) {
        this.id = id;
        this.recurso = recurso;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.nombreUsuario = nombreUsuario;
        this.estado = EstadoReserva.CONFIRMADA;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    /**
     * Verifica si esta reserva se solapa con otra reserva
     * @param otra la otra reserva a comparar
     * @return true si hay solapamiento de horarios
     */
    public boolean seSolapaCon(Reserva otra) {
        // Solo pueden solaparse si son del mismo recurso y fecha
        if (!this.recurso.equals(otra.recurso) || !this.fecha.equals(otra.fecha)) {
            return false;
        }

        // Solo consideramos reservas confirmadas
        if (this.estado != EstadoReserva.CONFIRMADA || otra.estado != EstadoReserva.CONFIRMADA) {
            return false;
        }

        // Verificar solapamiento de horarios
        // Hay solapamiento si:
        // - esta reserva comienza antes de que la otra termine Y
        // - esta reserva termina después de que la otra comience
        return this.horaInicio.isBefore(otra.horaFin) && this.horaFin.isAfter(otra.horaInicio);
    }
}
