package com.ks;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        int puerto = 9000;

        servidorTCP servidores[] = new servidorTCP[10];
        for(servidorTCP servidor: servidores)
        {
            servidor = new servidorTCP();
            servidor.setPuerto(puerto);
            servidor.setDaemon(false);
            servidor.conectar();
            puerto++;
        }

        puerto = 1000;
        clienteTCP clientes[] = new clienteTCP[4];
        for(clienteTCP cliente: clientes)
        {
            cliente = new clienteTCP();
            cliente.setIP("localhost");
            cliente.setPuerto(puerto);
            cliente.conectar();
            puerto += 1000;
        }
    }
}
