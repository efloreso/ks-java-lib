package ks.tcp;

import ks.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Servidor extends Thread implements Tcp {
    //Variables de clase
    private int VMintPuerto;
    private int VMintClientes;

    private Log VMksDepuracion;
    private ServerSocket VMnetServidor;
    private List<Cliente> VMlstClientes;
    private boolean VMblnDetener;

    private EventosTCP VMtcpEventos;

    //Constructor
    public Servidor() {
        VMintPuerto = 0;
        VMintClientes = 0;
        VMksDepuracion = Log.getInstancia();
        VMblnDetener = false;
        this.setDaemon(true);
        VMlstClientes = new LinkedList<Cliente>();
    }

    //Propiedades
    public int getPuerto() {
        return VMintPuerto;
    }

    public int getClientesConectados() {
        return VMintClientes;
    }

    public void setPuerto(int puerto) {
        VMintPuerto = puerto;
    }

    public void setEventos(EventosTCP eventos) {
        VMtcpEventos = eventos;
    }

    public void conectar() {
        try {
            if (VMintPuerto != 0) {
                VMnetServidor = new ServerSocket(VMintPuerto);
                if (VMnetServidor.isBound()) {
                    this.start();
                }
            } else {
                VMtcpEventos.errorConexion("No se ha establecido el puerto de escucha");
            }
        } catch (Exception ex) {
            VMtcpEventos.errorConexion("No se ha establecido el puerto de escucha: " + ex.getMessage());
        }
    }

    public void run() {
        try {
            do {
                Socket VLobjDatos = VMnetServidor.accept();
                try {
                    Cliente VLtcpCliente = new Cliente();
                    VLtcpCliente.setEventos(VMtcpEventos);
                    VLtcpCliente.setCliente(VLobjDatos);
                    VMlstClientes.add(VLtcpCliente);
                } catch (Exception ex) {
                    VMksDepuracion.agregarMensaje("Problema al escuchar al cliente: " + ex.getMessage());
                }
            } while (!VMblnDetener);
        } catch (Exception ex) {
            VMksDepuracion.agregarMensaje("Problema al mantener el puerto en escucha: " + ex.getMessage());
        }
    }

    @Override
    public void setIP(String IP) {
        // TODO Auto-generated method stub

    }

    @Override
    public void enviar(String mensaje) {
        // TODO Auto-generated method stub
        for (Cliente VLtcpCliente : VMlstClientes) {
            try
            {
                VLtcpCliente.enviar(mensaje);
            }
            catch (Exception ex)
            {

            }
        }
    }
}
