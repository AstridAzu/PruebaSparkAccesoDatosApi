
/*
API REST BASICA -JAVA CON SPARK
aplicacion simple que proporcione un endpoint tipoGet para obtener una salida
en el servidor se inicia en el puerto 4567 con un mensaje de texto plano
* */


import static spark.Spark.*;

public class ejercicio2Spark {
    public static void main(String[] args) {
        //como se levanta el puerto
        port(4567);
        get("/",(req,res)->{
            return "¿hola desde Spark?";
        });
    }
}
