package models;
/*
MODELO ES COMO UNA TABLA UNA CARPETA DE MODELOS REPRESTANDO CADA
UNA DE ESAS TABLAS DE FORMATO DE OBJETO
*/


public class Producto {
  private int id;
  private String nombre;
  private Double  precio;

  public Producto() {}

  //constructor
    public Producto(int id, String nombre, Double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;

     //los set y get
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                '}';
    }
}
