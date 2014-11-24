package ks;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class Log {
    private static Log VMobjLog;
    private Queue<String> VMcolaMensajes;
    private SimpleDateFormat VMstrFecha;
    private SimpleDateFormat VMstrFechaHora;
    private Thread VMhiloEscribir;
    private String VMstrPath;
    private static String VMstrConfiguracion;

    private Log() {
        if (this.VMhiloEscribir == null) {
            this.VMhiloEscribir = new Thread();
            VMhiloEscribir.setDaemon(true);
            this.VMstrFecha = new SimpleDateFormat("yyMMdd");
            this.VMstrFechaHora = new SimpleDateFormat("yyMMdd HH:mm:ss");
            this.VMcolaMensajes = new LinkedList<String>();
            if (Configuracion.getRuta().contains("/")) {
                this.VMstrPath = (Configuracion.getRuta() + "/log/");
            } else {
                this.VMstrPath = (Configuracion.getRuta() + "\\log\\");
            }
            VMstrConfiguracion = Configuracion.getConfiguracion();
            File VLioCarpeta = new File(this.VMstrPath);
            if (!VLioCarpeta.exists()) {
                VLioCarpeta.mkdir();
            }
        }
    }

    public static Log getInstancia() {
        if (VMobjLog == null)
            VMobjLog = new Log();
        return VMobjLog;
    }

    public void agregarMensaje(String Mensaje) {
        try {
            VMcolaMensajes.add(Mensaje);
            if (!VMhiloEscribir.isAlive()) {
                VMhiloEscribir = new Thread() {
                    @Override
                    public void run() {
                        String VLstrMensaje;
                        boolean VLblnEscribi;
                        FileOutputStream VLioArchivo;

                        try {
                            Date VMdateNow = new Date();
                            VLioArchivo = new FileOutputStream(VMstrPath + VMstrConfiguracion + "-" + VMstrFecha.format(VMdateNow) + ".log", true);
                            do {
                                try {
                                    synchronized (VMcolaMensajes) {
                                        VLstrMensaje = VMstrFechaHora.format(VMdateNow).toString() + ":" + VMcolaMensajes.poll() + "\n";
                                    }
                                    VLblnEscribi = false;
                                    do {
                                        try {
                                            VLioArchivo.write(VLstrMensaje.getBytes());
                                            VLioArchivo.flush();
                                            VLblnEscribi = true;
                                        } catch (Exception ex) {
                                            if (VLioArchivo != null) {
                                                VLioArchivo.flush();
                                                VLioArchivo.close();
                                            }
                                            Thread.sleep(500);
                                            VLioArchivo = new FileOutputStream(VMstrPath + VMstrConfiguracion + "-" + VMstrFecha.format(VMdateNow) + ".log", true);
                                        }
                                    } while (!VLblnEscribi);
                                } catch (Exception ex) {
                                    break;
                                }
                            } while (VMcolaMensajes.size() >= 1);
                            VLioArchivo.close();
                        } catch (Exception ex) {

                        }
                    }

                };
                VMhiloEscribir.setName("Mensaje depuracion");
                VMhiloEscribir.setDaemon(false);
                VMhiloEscribir.setPriority(1);
                VMhiloEscribir.start();
            }
        } catch (Exception ex) {

        }
    }
}
