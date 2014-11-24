package ks.tcp.protocolos;

/**
 * Created by Miguel on 24/03/14.
 */

import ks.Log;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Iso {
    private String Encabezado;
    private String Tipo;
    private String Primario;
    private String Secundario;
    public String Campos[];

    private static final Log VMobjDepuracion;

    static {
        VMobjDepuracion = Log.getInstancia();
    }

    //Constructor
    public Iso() {
        Encabezado = "";
        Tipo = "";
        Primario = "";
        Secundario = "";
        Campos = new String[127];
    }

    //Propiedades
    public String getEncabezado() {
        return this.Encabezado;
    }

    public String getTipo() {
        return this.Tipo;
    }

    public String getPrimario() {
        return this.Primario;
    }

    public String getSecundario() {
        return this.Secundario;
    }

    //Metodos
    public static String mapeo(String Mapeo) {
        String VLstrRespuesta = "";
        int i, VLintValor, VLintLongitud;

        if (Mapeo.startsWith("1")) {
            VLintLongitud = Mapeo.length();
        } else {
            VLintLongitud = Mapeo.length() / 2;
        }
        for (i = 0; i < VLintLongitud; i += 4) {
            VLintValor = Integer.parseInt(Mapeo.substring(i, i + 4), 2);
            VLstrRespuesta += Integer.toHexString(VLintValor);
        }
        return VLstrRespuesta.toUpperCase();
    }

    public boolean procesarISO(String Transaccion) {
        int i = 0, VLintValor, VLintPosicion, VLintLongitud;

        NumberFormat VLobjFormat = new DecimalFormat("0000");
        try {
            this.Encabezado = Transaccion.substring(0, 12);
            this.Tipo = Transaccion.substring(12, 16);
            for (i = 0; i < 16; i++) {
                VLintValor = Integer.parseInt(Transaccion.substring(16 + i, 17 + i), 16);
                this.Primario += VLobjFormat.format(Long.parseLong(Integer.toBinaryString(VLintValor)));
            }
            VLintPosicion = 32;
            if (this.Primario.startsWith("1")) {
                for (i = 0; i < 16; i++) {
                    VLintValor = Integer.parseInt(Transaccion.substring(VLintPosicion, VLintPosicion + 1), 16);
                    this.Secundario += VLobjFormat.format(Long.parseLong(Integer.toBinaryString(VLintValor)));
                    VLintPosicion++;
                }
            }
            for (i = 1; i < this.Primario.length(); i++) {
                if (this.Primario.substring(i, i + 1).equals("1")) {
                    switch (i + 1) {
                        case 3:
                            VLintLongitud = 6;
                            break;
                        case 4:
                            VLintLongitud = 12;
                            break;
                        case 7:
                            VLintLongitud = 10;
                            break;
                        case 11:
                            VLintLongitud = 6;
                            break;
                        case 12:
                            VLintLongitud = 6;
                            break;
                        case 13:
                            VLintLongitud = 4;
                            break;
                        case 15:
                            VLintLongitud = 4;
                            break;
                        case 17:
                            VLintLongitud = 4;
                            break;
                        case 18:
                            VLintLongitud = 4;
                            break;
                        case 22:
                            VLintLongitud = 3;
                            break;
                        case 23:
                            VLintLongitud = 3;
                            break;
                        case 32:
                            VLintLongitud = 2 + Integer.parseInt(Transaccion.substring(VLintPosicion, VLintPosicion + 2));
                            break;
                        case 35:
                            VLintLongitud = 2 + Integer.parseInt(Transaccion.substring(VLintPosicion, VLintPosicion + 2));
                            break;
                        case 37:
                            VLintLongitud = 12;
                            break;
                        case 38:
                            VLintLongitud = 6;
                            break;
                        case 39:
                            VLintLongitud = 2;
                            break;
                        case 41:
                            VLintLongitud = 16;
                            break;
                        case 43:
                            VLintLongitud = 40;
                            break;
                        case 48:
                            VLintLongitud = 3 + Integer.parseInt(Transaccion.substring(VLintPosicion, VLintPosicion + 3));
                            break;
                        case 49:
                            VLintLongitud = 3;
                            break;
                        case 54:
                            VLintLongitud = 3 + Integer.parseInt(Transaccion.substring(VLintPosicion, VLintPosicion + 3));
                            break;
                        case 55:
                            VLintLongitud = 3 + Integer.parseInt(Transaccion.substring(VLintPosicion, VLintPosicion + 3));
                            break;
                        case 63:
                            VLintLongitud = 3 + Integer.parseInt(Transaccion.substring(VLintPosicion, VLintPosicion + 3));
                            break;
                        case 90:
                            VLintLongitud = 42;
                            break;
                        default:
                            continue;
                    }
                    try
                    {
                        this.Campos[i + 1] = Transaccion.substring(VLintPosicion, VLintPosicion + VLintLongitud);
                        VLintPosicion += VLintLongitud;
                    }
                    catch (Exception ex)
                    {
                        this.Campos[i + 1] = Transaccion.substring(VLintPosicion);
                        VMobjDepuracion.agregarMensaje("Transaccion incompleta, problema al obtener el ISO: " + ex.getMessage() + "\nTransaccion: " + Transaccion + "\nCampo: " + i + 1);
                        break;
                    }
                }
            }
        } catch (NumberFormatException ex) {
            VMobjDepuracion.agregarMensaje("Problema al obtener el ISO: " + ex.getMessage() + "\nTransaccion: " + Transaccion + "\nCampo: " + i + 1);
        }
        return true;
    }

    public static synchronized int longitudISO(String len) {
        try {
            byte[] VLbyteDatos;
            int VLintRespuesta;
            VLbyteDatos = len.getBytes("ISO-8859-1");
            String VLstrHex = String.format("%02X", VLbyteDatos[1]);
/*            if (VLstrHex.trim().length() == 1) {
                VLstrHex = "0" + VLstrHex;
            }*/
            VLstrHex = String.format("%02X", VLbyteDatos[0]) + VLstrHex;
            VLintRespuesta = Integer.parseInt(VLstrHex, 16);
            return VLintRespuesta;
        } catch (UnsupportedEncodingException ex) {
            VMobjDepuracion.agregarMensaje("Problema al obtener la longitud del ISO: " + ex.getMessage());
            return 0;
        }
    }

    public static synchronized int longitudISO(int valor1, int valor2) {
        try {
            int VLintTotal;
            if (valor1 < 0)
            {
                valor1 = 256 + valor1;
            }
            if (valor2 < 0)
            {
                valor2 = 256 + valor2;
            }
            VLintTotal = valor1 << 8;
            VLintTotal += valor2;
            return VLintTotal;
        } catch (Exception ex) {
            VMobjDepuracion.agregarMensaje("Problema al obtener la longitud del ISO: " + ex.getMessage());
            return 0;
        }
    }

    public static synchronized String obtenerLongitud(int Longitud) {
        NumberFormat VLobjFormat = new DecimalFormat("0000000000000000");
        String VLstrBinario = VLobjFormat.format(Double.parseDouble(Integer.toBinaryString(Longitud)));
        byte VLbyteDatos[] = new byte[2];
        VLbyteDatos[0] = (byte) Integer.parseInt(VLstrBinario.substring(0, 8), 2);
        VLbyteDatos[1] = (byte) Integer.parseInt(VLstrBinario.substring(8, 16), 2);
        return new String(VLbyteDatos);
    }
}
