package com.framework.report;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO con los datos de un test que el renderer serializa a JSON
 * y el template HTML consume vía JavaScript.
 *
 * Los nombres de campo se mapean 1:1 con las claves esperadas por
 * report-template.html (no renombrar sin actualizar el JS).
 */
public class TestResultData {

    public int id;
    public String name;
    public String className;
    public String status;          // "pass" | "fail" | "skip"
    public String time;            // "HH:mm:ss"
    public double dur;             // segundos
    public List<String> tags = new ArrayList<>();
    public List<EventEntry> events = new ArrayList<>();
    public String err;             // stack trace en caso de fallo; null si no aplica
    public String screenshotPath;  // ruta relativa al HTML; null si no hay

    /** Marca de tiempo de inicio en millis para calcular duración al final. */
    public transient long startMillis;

    public static class EventEntry {
        public String s;  // "info" | "pass" | "fail"
        public String t;  // "HH:mm:ss.SSS"
        public String d;  // descripción (HTML permitido para <code>)

        public EventEntry() { }

        public EventEntry(String s, String t, String d) {
            this.s = s; this.t = t; this.d = d;
        }
    }
}
