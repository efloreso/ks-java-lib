package ks.tcp;

import java.util.EventListener;

public interface EventosTCP extends EventListener {
    void conexionEstablecida(Cliente cliente);

    void errorConexion(String mensaje);

    void datosRecibidos(String mensaje, byte[] datos, Tcp cliente);

    void cerrarConexion(Cliente cliente);
}
