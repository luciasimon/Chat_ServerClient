import java.util.ArrayList;

public class Colores {
     String black="\033[30m";
     String red="\033[31m";
     String green="\033[32m";
    String yellow="\033[33m";
    String blue="\033[34m";
    String purple="\033[35m";
    static String cyan="\033[36m";
    static String white="\033[37m";
    static String reset="\u001B[0m";

    public static ArrayList<String> listaColores = new ArrayList<>();
    public Colores() {
        listaColores.add(red);
        listaColores.add(green);
        listaColores.add(yellow);
        listaColores.add(blue);
        listaColores.add(purple);
        listaColores.add(cyan);
    }

}
