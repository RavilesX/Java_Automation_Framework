package com.framework.commonaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Clase base con utilidades comunes reutilizables por todos los Page Objects y test cases.
 *
 * Equivalente a commonaction/other_actions.py del framework Python.
 * En Python, CommonActions extiende softest.TestCase para habilitar soft asserts.
 * En Java, los soft asserts son provistos directamente por TestNG (SoftAssert),
 * por lo que esta clase no necesita extender ninguna clase base.
 */
public class CommonActions {

    /**
     * Crea y devuelve un Logger usando el nombre de la clase llamante como identificador.
     *
     * Equivalente al logger() estático de Python, que usa inspect.stack()[1][3]
     * para obtener el nombre del método/clase que lo invoca.
     *
     * Uso típico en Page Objects y test cases:
     *   private static final Logger log = CommonActions.getLogger();
     *
     * @return Logger de Log4j2 identificado con el nombre de la clase llamante.
     */
    public static Logger getLogger() {
        // getStackTrace()[0] = getStackTrace()
        // getStackTrace()[1] = getLogger()  (este método)
        // getStackTrace()[2] = la clase que llamó getLogger()
        String callerClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        return LogManager.getLogger(callerClassName);
    }
}
