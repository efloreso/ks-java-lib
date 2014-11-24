package ks.transaccion;

/**
 * Created by Miguel on 27/03/14.
 */
public interface LogTransaccional
{
    abstract void setRuta(String ruta);
    abstract void setNomenclatura(String nombre);

    abstract void abrir();
    abstract void agregar(String mensaje);
}
