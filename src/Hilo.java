import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Hilo extends Thread {
    private Socket socketCliente;
    ObjectOutputStream streamSalidaObj;
    public static ArrayList<Hilo> listaClientes = new ArrayList<>();
    public static ArrayList<String> listaUsuariosConectados = new ArrayList<>();
    public static LinkedList<Mensaje> historialMensajes = new LinkedList<>();
    public static ArrayList<String> coloresUsados = new ArrayList<>();
    final int MAXIMO_MENSAJES = 50;
    String nombreUsuario;
    String colour;

    public Hilo(Socket socketCliente) {
        try {
            this.socketCliente = socketCliente;
            this.streamSalidaObj = new ObjectOutputStream(socketCliente.getOutputStream());
            listaClientes.add(this);
        } catch (IOException e) {
            System.out.println("-- Error al crear el hilo --");
        }
    }

    @Override
    public void run() {
        try {
            //cogemos el nombre del usuario
            InputStream inputStream = socketCliente.getInputStream();
            byte[] nombre = new byte[24];
            int numBytes = inputStream.read(nombre);
            nombreUsuario = new String(nombre, 0, numBytes);
            listaUsuariosConectados.add(nombreUsuario);
            String clienteNuevo = "-- " + nombreUsuario + " se unió al chat --";
            Mensaje m = new Mensaje(clienteNuevo);
            System.out.println(clienteNuevo);
            broadcast(m);

            //cargamos el historial en cuanto se ha unido al chat
            cargarMensajesAnteriores();

            //generamos el color
            colour = asignarColor(coloresUsados, Colores.listaColores);

            ObjectInputStream streamEntradaObj = new ObjectInputStream(socketCliente.getInputStream());
            while (true) {
                //leemos el objeto Mensaje que nos llega
                Mensaje mensajeRecibido = (Mensaje) streamEntradaObj.readObject();

                //gestionamos la salida del cliente con la palabra SALIR
                if (mensajeRecibido.getContenido().equals("SALIR")) {
                    socketCliente.close();
                    listaUsuariosConectados.remove(nombreUsuario);
                    coloresUsados.remove(mensajeRecibido.getColor());
                    Colores.listaColores.add(mensajeRecibido.getColor());
                    break;
                }

                mensajeClienteAlServidor(nombreUsuario, mensajeRecibido.getContenido());
                String mensaje = mensajeRecibido.getContenido();

                //reenviamos el Mensaje
                String finalColour = colour;
                new Thread(() -> {
                    Mensaje mensajeColor = new Mensaje(nombreUsuario, mensaje, finalColour);

                    //gestión del historial
                        //si no hay 50 mensajes, se añade
                    if(!mensajeColor.getContenido().startsWith("@")) {
                        if (historialMensajes.size() < MAXIMO_MENSAJES) {
                            historialMensajes.addLast(mensajeColor);
                        } else {
                            //si ya hay 50 mensajes, se borra uno y se añade el nuevo
                            historialMensajes.removeFirst();
                            historialMensajes.addLast(mensajeColor);
                        }
                    }

                    //gestionamos los mensajes privados
                    if (mensaje.startsWith("@")) {
                        try {
                            //cogemos el nombre del ususario al que va dirigido el mensaje privado
                            int espacio = mensaje.indexOf(' ', mensaje.indexOf("@"));
                            String destinatarioPrivado = mensaje.substring(mensaje.indexOf("@") + 1, espacio);
                            boolean usuarioEncontrado = false;

                            //se lo enviamos sólo a él
                            for (Hilo h : listaClientes) {
                                if (destinatarioPrivado.equals(h.nombreUsuario)) {
                                    Mensaje mensajePrivado = new Mensaje(nombreUsuario, mensaje, finalColour);
                                    h.streamSalidaObj.writeObject(mensajePrivado);
                                    h.streamSalidaObj.flush();
                                    usuarioEncontrado = true;
                                    break;
                                }
                            }
                            if (!usuarioEncontrado) {
                                System.out.println("Ups, ese usuario no se ha encontrado");
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        broadcast(mensajeColor);
                    }
                }).start();

            }
        } catch (IOException | ClassNotFoundException e) {
            String m = "-- " + nombreUsuario + " se ha desconectado --";
            Mensaje mensaje = new Mensaje(m);
            System.out.println(mensaje.getContenido());

            listaUsuariosConectados.remove(nombreUsuario);
            Colores.listaColores.remove(colour);
            listaClientes.remove(this);

            broadcast(mensaje);
        }
    }

    /**
     * Método que imprimirá los mensajes con el formato "[usuario]: mensaje"
     * @param nombreUsuario
     * @param mensaje
     * @return mensaje con el formato aplicado
     */
    public String mensajeClienteAlServidor(String nombreUsuario, String mensaje) {
        String m = "[" + nombreUsuario + "]: " + mensaje;
        System.out.println(m);
        return m;
    }

    /**
     * Método para reenviar a todos los clientes el mensaje
     * @param mensaje mensaje a enviar
     */
    public void broadcast(Mensaje mensaje) {
        try {
            for (Hilo hilo : listaClientes) {
                if (!nombreUsuario.equals(hilo.nombreUsuario) && hilo.isAlive()) {
                    hilo.streamSalidaObj.writeObject(mensaje);
                    hilo.streamSalidaObj.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("Ups, algo en el broadcast de mensajes ha fallado");
        }
    }

    /**
     * Método para cargar el historial de los últimos mensajes
     */
    public void cargarMensajesAnteriores() {
        for (Mensaje m : historialMensajes) {
            try {
                streamSalidaObj.writeObject(m);
                streamSalidaObj.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Método para asignar un color aleatorio al usuario
     * @param coloresAsignados lista de colores usándose
     * @param coloresDisponibles lista de colores que están libres
     * @return color
     */
    public static String asignarColor(ArrayList<String> coloresAsignados, ArrayList<String> coloresDisponibles) {
        if (coloresDisponibles.isEmpty()) {
            return Colores.white;
        }
        Random random = new Random();
        int index = random.nextInt(coloresDisponibles.size());
        String color = coloresDisponibles.get(index);
        coloresDisponibles.remove(index);
        coloresAsignados.add(color);
        return color;
    }

}
