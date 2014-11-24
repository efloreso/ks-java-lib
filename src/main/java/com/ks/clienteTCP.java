package com.ks;

import ks.tcp.Cliente;
import ks.tcp.EventosTCP;
import ks.tcp.Tcp;

/**
 * Created by Miguel on 24/11/2014.
 */
public class clienteTCP extends Cliente implements EventosTCP
{
    public clienteTCP()
    {
        super();
        this.setEventos(this);
    }
    @Override
    public void conexionEstablecida(Cliente cliente) {

    }

    @Override
    public void errorConexion(String mensaje) {

    }

    @Override
    public void datosRecibidos(String mensaje, byte[] datos, Tcp cliente) {
        servidorTCP.enviarTodos(mensaje);
    }

    @Override
    public void cerrarConexion(Cliente cliente) {

    }
}
