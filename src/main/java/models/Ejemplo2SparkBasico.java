package models;

import spark.Spark;

//importacionde de libreria
import static spark.Spark.*;


/*
APY REST BASICA -ENJAVA CON SPARK
aplicacion simple que  proporciona un  endpoint tipoGET para optener un saludo
el servidor se inicia en el puerto 4567 con  un mensaje de texto plano
 */


public class Ejemplo2SparkBasico {
    public static void main(String[] args) {
        //como se levanta el puerto
          port(4567)    ;

          get("/",(rep, res) ->{
              return "¿hola desde Spark!?";
          });

          System.out.println("Servidor iniciado en: http://localhost:4567");
    }


}
