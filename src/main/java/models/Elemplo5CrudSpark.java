package models;

import com.google.gson.Gson;
import models.Producto;
import spark.Spark;

import static spark.Spark.*;
import java.util.Map;
import java.util.HashMap;
/*
Ejemplo 5:completar comn spark
Apy
 */


public class Elemplo5CrudSpark {
    //inicalizar vqriables y objetos
    private static Map<Integer, Producto> productos= new   HashMap<>();
    private static int siguienteId = 1;
    private  static Gson gson = new Gson();

    public static void main(String[] args) {
        port(4567);


        //
        productos.put(1, new Producto(1, "portatil" , 999.99));
        siguienteId = 2;


        get("/productos", (req,res)->{
            res.type("application/json");
            return productos.values();
                }, gson::toJson);
        //get todos los productos
        get("/productos/:id" , (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Producto producto =productos.get(id);
            if(producto==null){
                res.type("application/json");
                return productos;
            }else{
                res.status(404);
                return Map.of("Error", "Producto no encontrado");
            }

        }, gson::toJson);

        post("/productos", (req, res)->{
            Producto producto = gson.fromJson(req.body(), Producto.class);
            producto.setId(siguienteId++);
            productos.put(producto.getId(), producto);
            res.status(201);
            res.type("application/json");
            return producto;

        }, gson::toJson);
// PUT - Actualizar el producto
        put("/productos/:id", (req, res) ->{
            int id = Integer.parseInt(req.params("id"));
            Producto producto = gson.fromJson(req.body(), Producto.class);
            producto.setId(id);
            productos.put(producto.getId(), producto);
            res.type("application/json");
            return producto;
        }, gson::toJson);
        //DELETE - Eliminar el producto
        delete("/productos/:id", (req, res) ->{
            int id = Integer.parseInt(req.params("id"));
            productos.remove(id);
            res.status(204);
            return "";
        });

        System.out.println("Servidor iniciado en: http://localhost:4567");
        System.out.println("Endpoints disponibles: ");
        System.out.println("GET /productos");
        System.out.println("GET /productos/:id");
        System.out.println("Pos /productos/:id");
        System.out.println("Put /productos/:id");
        System.out.println("Delete /productos/:id");

    }

}
