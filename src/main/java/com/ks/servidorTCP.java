package com.ks;

import ks.tcp.Cliente;
import ks.tcp.EventosTCP;
import ks.tcp.Servidor;
import ks.tcp.Tcp;

import java.util.ArrayList;

/**
 * Created by Miguel on 24/11/2014.
 */
public class servidorTCP extends Servidor implements EventosTCP
{
    private static ArrayList<servidorTCP> servidores;

    static
    {
        servidores = new ArrayList<servidorTCP>();
    }

    public servidorTCP()
    {
        super();
        this.setEventos(this);
        servidores.add(this);
    }
    @Override
    public void conexionEstablecida(Cliente cliente) {

    }

    @Override
    public void errorConexion(String mensaje) {

    }

    @Override
    public void datosRecibidos(String mensaje, byte[] datos, Tcp cliente) {

    }

    @Override
    public void cerrarConexion(Cliente cliente) {

    }

    public static void enviarTodos(String mensaje)
    {
        for(servidorTCP servidor: servidores)
        {
            servidor.enviar(mensaje);
        }
    }
}
