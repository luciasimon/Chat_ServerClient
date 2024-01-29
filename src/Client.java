import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try {
            mensajeCliente("Presiona Enter para empezar");
            Scanner sn = new Scanner(System.in);
            sn.nextLine();

            //pedimos nombre y lo mandamos al hilo del servidor
            mensajeCliente("¿Cómo te llamas?");
            String nombre = sn.nextLine();

            //Creamos el socket y establecemos conexión
            /*mensajeCliente("Creamos socket");*/
            Socket clientSocket = new Socket();
            /*mensajeCliente("Establecemos conexión");*/
            InetSocketAddress address = new InetSocketAddress("localhost", 5555);
            clientSocket.connect(address);

            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(nombre.getBytes());
            outputStream.flush();

            mensajeCliente("Ya puedes escribir!");
            ObjectInputStream streamEntradaObj = new ObjectInputStream(clientSocket.getInputStream());
            //leemos mensajes
            Thread lectura = new Thread(() -> {
                try {
                    while (true) {
                        Mensaje mensajeCliente = (Mensaje) streamEntradaObj.readObject();
                        if (mensajeCliente.getContenido().startsWith("@")) {
                            mensajePrivadoRecibido(mensajeCliente.getUsuario(), mensajeCliente.getContenido(), mensajeCliente.getColor());
                        } else if(mensajeCliente.getContenido().equals("SALIR")){
                            clientSocket.close();
                            System.out.println("-- "+mensajeCliente.getUsuario()+" se ha desconectado --");
                        }else {
                            mensajeRecibido(mensajeCliente.getUsuario(), mensajeCliente.getContenido(), mensajeCliente.getColor());
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("--Adiós!--");
                }
            });
            lectura.start();

            //mandamos mensajes
            ObjectOutputStream streamSalidaObj = new ObjectOutputStream(clientSocket.getOutputStream());
            while (clientSocket.isConnected()) {
                String input = sn.nextLine();
                if (input.equals("SALIR")) {
                    clientSocket.close();
                    break;
                } else {
                    Mensaje mensaje = new Mensaje(input);
                    streamSalidaObj.writeObject(mensaje);
                    streamSalidaObj.flush();
                }
            }

        } catch (IOException e) {
            System.out.println("Uy, algo ha salido mal");
        }
    }

    private static void mensajeCliente(String mensaje) {
        System.out.println("CLIENTE: " + mensaje);
    }

    /**
     * Método que gestiona las variedades del mensaje recibido:
     * 1. Si el nombre de usuario es nulo, no se imprimirá su nombre así "[usuario]"
     * 2. Si el color asignado al usuario es blanco, se imprimirá sin color
     * 3. Si el color es nulo, se imprimirá sin color
     * 4. Si nada es nulo, se imprimirá con el formato especial que le añade el color
     * @param nombreUsuario
     * @param mensaje
     * @param color
     * @return mensaje
     */
    public static String mensajeRecibido(String nombreUsuario, String mensaje, String color) {
        String m;
        if(nombreUsuario == null){
            m = "\t\t\t\t\t\t"+ mensaje;
            System.out.println(m);
        }else if (color != null && color.equals(Colores.white)) {
            m = "\t\t\t\t\t\t[" + nombreUsuario + "]: " + mensaje;
            System.out.println(m);
        } else if(color!=null){
            m = color + "\t\t\t\t\t\t[" + nombreUsuario + "]: " + mensaje + Colores.reset;
            System.out.println(m);
        }else{
            m = "\t\t\t\t\t\t[" + nombreUsuario + "]: " + mensaje;
            System.out.println(m);
        }
        return m;
    }

    /**
     * Método que da un formato especial a los mensajes privados recibidos
     * @param nombreUsuario
     * @param mensaje
     * @param color
     * @return mensaje
     */
    public static String mensajePrivadoRecibido(String nombreUsuario, String mensaje, String color) {
        String m = color + "\t\t\t\t\t\t[" + nombreUsuario + " PRIVADO]: " + mensaje + Colores.reset;
        System.out.println(m);
        return m;
    }
}
