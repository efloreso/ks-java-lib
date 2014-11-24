package ks;

import java.io.File;


public class Configuracion {
    private static String VMstrConfiguracion;
    private static String VMstrPath;
    private static String VMstrPathConf;
    private static boolean VMblnActivaLog;

    static {
        VMblnActivaLog = false;
        VMstrConfiguracion = "";
    }

    public static String getRuta() {
        if (VMstrPath == null) {
            //VMstrPath = Configuracion.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File currentDirectory = new File(new File(".").getAbsolutePath());
            VMstrPath = currentDirectory.getAbsolutePath();
            VMstrPath = VMstrPath.replaceAll("%20", " ");
            if (VMstrPath.contains("/")) {
                VMstrPath = VMstrPath.substring(0, VMstrPath.lastIndexOf("/")) + "/";
            } else {
                VMstrPath = VMstrPath.substring(0, VMstrPath.lastIndexOf("\\")) + "\\";
            }
        }
        return VMstrPath;
    }

    public static String getRutaConfiguracion() {
        if (VMstrPathConf == null) {
            if (VMstrPath == null) {
                Configuracion.getRuta();
            }
            if (VMstrPath.contains("/")) {
                VMstrPathConf = VMstrPath + "config/" + VMstrConfiguracion + ".properties";
            } else {
                VMstrPathConf = VMstrPath + "config\\" + VMstrConfiguracion + ".properties";
            }
        }
        return VMstrPathConf;
    }

    public static boolean getActivarLog() {
        return VMblnActivaLog;
    }

    public static void setActivarLog() {
        VMblnActivaLog = true;
    }

    public static void setConfiguracion(String configuracion) {
        VMstrConfiguracion = configuracion;
    }

    public static String getConfiguracion() {
        return VMstrConfiguracion;
    }
}
