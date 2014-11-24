package ks.tcp;

import ks.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("ALL")
public class Cliente implements Tcp {
    private Socket VMnetCliente;

    protected int VMintPuerto;
    private int VMintReconectar;
    private static int VMintTimeOut;

    protected String VMstrIP;

    private boolean VMblnSalir;
    private boolean VMblnReconectar;

    private InputStream VMobjEntrada;
    private OutputStream VMobjSalida;

    private Log VMobjDepuracion;

    private EventosTCP VMobjEventos;

    private Thread VMhiloEscuchar;
    private Thread VMhiloEnviar;

    private Cliente VMtcpCliente;

    private Timer VMtmrReconectar;

    private Queue<String> VMcolaEnviar;

    //---------------
    //CONSTRUCTOR
    //----------------
    public Cliente() {
        VMintPuerto = 0;
        VMblnSalir = false;
        VMobjDepuracion = Log.getInstancia();
        VMintTimeOut = 0;
        VMhiloEscuchar = new Thread();
        VMtcpCliente = this;
        VMintReconectar = 30000;
        VMcolaEnviar = new LinkedList<String>();
        VMhiloEnviar = new Thread();
    }

    //----------------
    //PROPIEDADES
    //----------------
    public static void setTimeOut(int tiempo) throws Exception {
        VMintTimeOut = tiempo;
    }

    public void setCliente(Socket conexion) {
        try {
            VMnetCliente = conexion;
            this.activiarPropiedades();
            if (VMobjEventos != null) {
                VMobjEventos.conexionEstablecida(this);
            }
        } catch (Exception ex) {
            if (VMobjEventos != null) {
                VMobjEventos.errorConexion(ex.getMessage());
            }
        }
    }

    public void setPuerto(int puerto) {
        VMintPuerto = puerto;
    }

    public void setIP(String ip) {
        VMstrIP = ip;
    }

    public void setEventos(EventosTCP eventos) {
        VMobjEventos = eventos;
    }

    public void setReconectar(int tiempo) {
        VMintReconectar = tiempo;
    }

    //---------------
    //METODOS
    //---------------
    public void conectar() {
        try {
            VMblnReconectar = true;
            VMblnSalir = false;
            VMnetCliente = new Socket(VMstrIP, VMintPuerto);
            this.activiarPropiedades();
            if (VMobjEventos != null) {
                VMobjEventos.conexionEstablecida(this);
            }
        } catch (Exception ex) {
            if (VMobjEventos != null) {
                VMobjEventos.errorConexion(ex.getMessage());
            }
            reconectar();
        }
    }

    private void activiarPropiedades() throws IOException {
        VMnetCliente.setTcpNoDelay(true);
        VMnetCliente.setSoTimeout(VMintTimeOut);
        VMnetCliente.setPerformancePreferences(1, 0, 0);
        VMnetCliente.setKeepAlive(true);
        VMnetCliente.setReuseAddress(false);
        VMobjEntrada = VMnetCliente.getInputStream();
        VMobjSalida = VMnetCliente.getOutputStream();
        escuchar();
    }

    public synchronized void enviar(String mensaje) {
        VMcolaEnviar.add(mensaje);
        if (!VMhiloEnviar.isAlive())
        {
            VMhiloEnviar = new Thread()
            {
                public void run()
                {
                    String VLstrMensaje;
                    do {
                        synchronized (VMcolaEnviar)
                        {
                            VLstrMensaje = VMcolaEnviar.poll();
                        }
                        if (VMobjSalida != null) {
                            if (VMnetCliente.isConnected()) {
                                try {
                                    VMobjSalida.write(VLstrMensaje.getBytes());
                                    VMobjSalida.flush();
                                } catch (Exception ex) {
                                    if (VMobjEventos != null) {
                                        VMobjEventos.errorConexion("Problema al enviar datos: " + ex.getMessage());
                                    }
                                    if (VMblnReconectar) {
                                        reconectar();
                                    }
                                    cerrar();
                                }
                            }
                        }
                    }while (VMcolaEnviar.size() > 0);
                }
            };
            VMhiloEnviar.start();
        }
    }

    public void cerrar() {
        VMblnSalir = true;
        VMblnReconectar = false;
        try {
            VMobjEntrada.close();
        } catch (Exception ex) {

        }
        try {
            VMobjSalida.close();
        } catch (Exception ex) {

        }
        try {
            VMnetCliente.close();
        } catch (Exception ex) {

        }
        VMnetCliente = null;
        VMobjEntrada = null;
        VMobjSalida = null;
        VMobjDepuracion.agregarMensaje("Se cerro la conexion con: " + VMstrIP + ":" + VMintPuerto);
        VMobjEventos.cerrarConexion(this);
    }

    private void escuchar() {
        if (!VMhiloEscuchar.isAlive()) {
            while(VMobjEntrada == null)
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            VMhiloEscuchar = new Thread() {
                @Override
                public void run() {
                    byte VLbyteMensaje[], VLbyteTemp;
                    int VLintFaltantes;
                    String VLstrMensaje = "";
                    do {
                        try {
                            VLbyteMensaje = new byte[1];
                            VMobjEntrada.read(VLbyteMensaje, 0, 1);
                            VLintFaltantes = VMobjEntrada.available();
                            if (VLintFaltantes > 0)
                            {
                                VLbyteTemp = VLbyteMensaje[0];
                                VLbyteMensaje = new byte[VLintFaltantes + 1];
                                VLbyteMensaje[0] = VLbyteTemp;
                                VMobjEntrada.read(VLbyteMensaje, 1, VLintFaltantes);
                                VLstrMensaje = new String(VLbyteMensaje,0,VLbyteMensaje.length, "ISO-8859-1");
                            } else if (VLbyteMensaje[0] == 0) {
                                if (VMobjEventos != null) {
                                    VMobjEventos.errorConexion("Se desconecto por algun motivo");
                                }
                                VMblnSalir = true;
                                break;
                            }
                            VLintFaltantes = 0;
                            if (VMobjEventos != null) {
                                VMobjEventos.datosRecibidos(VLstrMensaje, VLbyteMensaje, VMtcpCliente);
                            }
                        } catch (Exception ex) {
                            if (VMobjEventos != null) {
                                VMobjEventos.errorConexion("Problema al recibir datos: " + ex.getMessage() + "\n" + ex.getStackTrace()[0].toString());
                            }
                            VMblnSalir = true;
                        }
                    } while (!VMblnSalir);
                    if (VMblnReconectar) {
                        reconectar();
                    }
                    cerrar();
                }
            };
            VMhiloEscuchar.setDaemon(false);
            VMhiloEscuchar.setName("Cliente: " + VMnetCliente.getLocalAddress().toString() + ":" + VMnetCliente.getPort());
            VMhiloEscuchar.start();
        }
    }

    public void reconectar() {
        if (VMtmrReconectar == null) {
            VMtmrReconectar = new Timer();
            VMtmrReconectar.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    VMobjDepuracion.agregarMensaje("Reconectando con : " + VMstrIP + ":" + VMintPuerto);
                    conectar();
                    if (VMnetCliente != null) {
                        if (VMnetCliente.isConnected()) {
                            VMtmrReconectar.cancel();
                            VMtmrReconectar = null;
                        }
                    }
                }
            }, VMintReconectar, VMintReconectar);
        }
    }
}
