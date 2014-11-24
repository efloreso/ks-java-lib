package ks;

/**
 * Created by Miguel on 4/04/14.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class xml
{

    private static xml VMobjLog;
    private static String VMstrConfiguracion;
    private Queue <String> VMcolaMensajes;
    private Escribir VMhiloEscribir;
    private Date VMdateFecha;
    private SimpleDateFormat VMstrFecha;
    private String VMstrPath;
    private Log VMobjDepuracion;

    public xml()
    {
        if ( VMhiloEscribir == null )
        {
            VMcolaMensajes = new LinkedList<String>();
            VMhiloEscribir = new Escribir();
            VMhiloEscribir.setDaemon(true);
            VMhiloEscribir.setName("Escribiendo log de transacciones");
            VMstrFecha = new SimpleDateFormat("yyMMdd");
            VMobjDepuracion = Log.getInstancia();
            if (Configuracion.getRuta().contains("/"))
            {
                this.VMstrPath = (Configuracion.getRuta() + "/log/");
            }
            else
            {
                this.VMstrPath = (Configuracion.getRuta() + "\\log\\");
            }
            File VLioCarpeta = new File (VMstrPath);
            if (!VLioCarpeta.exists())
                VLioCarpeta.mkdir();
        }
    }

    public static xml getInstancia()
    {
        if ( VMobjLog == null )
            VMobjLog = new xml();
        return VMobjLog;
    }

    public static void setConfiguracion(String configuracion)
    {
        VMstrConfiguracion = configuracion;
    }

    public synchronized void Agregar(String Mensaje)
    {
        try
        {
            VMcolaMensajes.add(Mensaje);
            if ( !VMhiloEscribir.isAlive() )
            {
                VMhiloEscribir = new Escribir();
                VMhiloEscribir.setName("Log tran");
                VMhiloEscribir.setDaemon(false);
                VMhiloEscribir.setPriority(1);
                VMhiloEscribir.start();
            }
        }
        catch ( Exception ex )
        {
            VMobjDepuracion.agregarMensaje("No se puede agregar el mensaje a la cola del xml: " + ex.getMessage() + "\nMensaje: " + Mensaje);
        }
    }

    private class Escribir extends Thread
    {
        @Override
        public void run ()
        {
            String VLstrMensaje, VLstrArchivo;
            FileOutputStream VLioArchivo;
            boolean VLblnEscribi;
            try
            {
                VLstrArchivo = VMstrConfiguracion + "-" + VMstrFecha.format(new Date()) + ".xml";
                File VLioExiste = new File(VMstrPath + VLstrArchivo);
                if ( !VLioExiste.exists() )
                {
                    VLioArchivo = new FileOutputStream (VMstrPath + VLstrArchivo, true );
                    String VLstrEncabezado = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
                    VLioArchivo.write(VLstrEncabezado.getBytes());
                }
                else
                    VLioArchivo = new FileOutputStream (VMstrPath + VLstrArchivo, true );
                VMdateFecha = new Date();
                do
                {
                    try
                    {
                        synchronized (VMcolaMensajes)
                        {
                            VLstrMensaje = VMcolaMensajes.poll() + "\n";
                        }
                        VLblnEscribi = false;
                        do
                        {
                            try
                            {
                                VLioArchivo.write(VLstrMensaje.getBytes());
                                VLioArchivo.flush();
                                VLblnEscribi = true;
                            } catch (IOException ex) {
                                VMobjDepuracion.agregarMensaje("No se puede agregar el mensaje al archivo xml: " + ex.getMessage() + "\nMensaje: " + VLstrMensaje);
                                VLioArchivo.flush();
                                VLioArchivo.close();
                                Thread.sleep(500);
                                VLioArchivo = new FileOutputStream (VMstrPath + VLstrArchivo, true );
                            }
                        } while (!VLblnEscribi);
                    }
                    catch ( IOException ex )
                    {
                        VMobjDepuracion.agregarMensaje("No se puede agregar el mensaje al archivo xml: " + ex.getMessage());
                        break;
                    } catch (InterruptedException ex) {
                        VMobjDepuracion.agregarMensaje("No se puede agregar el mensaje al archivo xml: " + ex.getMessage());
                        break;
                    }
                } while ( VMcolaMensajes.size() >= 1 );
                VLioArchivo.close();
                VMdateFecha = null;
            }
            catch(IOException ex)
            {
                VMobjDepuracion.agregarMensaje("Problema al guardar los mensaje de la cola al xml: " + ex.getMessage());
            }
        }
    }
}
