import java.io.Serializable;

public class Mensaje implements Serializable {
    public String usuario;
    public String contenido;
    public String color;

    public Mensaje(String contenido) {
        this.contenido = contenido;
    }

    public Mensaje(String usuario,String contenido, String color) {
        this.usuario = usuario;
        this.contenido = contenido;
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }


}
