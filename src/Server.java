import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            //Creamos el socket en el puerto
            int puertoEscucha = 5555;
            /*mensajeServidor("Creamos socket");*/
            ServerSocket serverSocket = new ServerSocket(puertoEscucha);
            /*mensajeServidor("Ahora acepto clientes juju");*/

            //Rellenamos la lista de colores. Lo hacemos en el servidor para que se haga una sola vez.
            Colores c = new Colores();

            while (true) {
                //para cada cliente, creamos un socket nuevo por el que hablaremos con ESE cliente, en un puerto nuevo
                Socket newSocket = serverSocket.accept();
                /*mensajeServidor("Conexión recibida. Hablaremos por el puerto " + newSocket.getPort());*/

                Hilo hilo = new Hilo(newSocket);
                hilo.start();
            }
        } catch (Exception e) {
            System.out.println("Algo salió mal con el servidor");
        }
    }

    public static String mensajeServidor(String mensaje) {
        String message = "SERVIDOR: " + mensaje;
        System.out.println(message);
        return message;
    }
}