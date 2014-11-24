package ks.tcp;

public interface Tcp {
    public abstract void setIP(String IP);

    public abstract void setPuerto(int Puerto);

    public abstract void setEventos(EventosTCP eventos);

    public abstract void conectar();

    public abstract void enviar(String mensaje);
}
