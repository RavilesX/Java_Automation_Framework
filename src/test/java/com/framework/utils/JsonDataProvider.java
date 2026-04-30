package com.framework.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilidad para cargar datos de prueba desde archivos JSON y convertirlos en
 * el formato Object[][] requerido por @DataProvider de TestNG.
 *
 * Equivalente a la combinación de @ddt + @file_data del framework Python.
 *
 * Formato esperado del JSON (mismo que el framework Python):
 * {
 *   "test1": { "param1": "valor1", "param2": "valor2", "start": ">>> START", "end": ">>> END" },
 *   "test2": { "param1": "valorA", "param2": "valorB", "start": ">>> START", "end": ">>> END" }
 * }
 *
 * Cada entrada del JSON se convierte en una fila del @DataProvider.
 * El test recibe un Map&lt;String, String&gt; con todos los campos del objeto JSON.
 *
 * Los archivos JSON deben ubicarse en: src/test/resources/testdata/
 */
public class JsonDataProvider {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Lee un archivo JSON desde src/test/resources/testdata/ y lo convierte
     * en Object[][] para usarse con @DataProvider de TestNG.
     *
     * Cada objeto JSON de primer nivel se convierte en una fila: Object[]{Map&lt;String, String&gt;}.
     * El test method debe declarar Map&lt;String, String&gt; como único parámetro.
     *
     * @param filename Nombre del archivo JSON (relativo a testdata/).
     *                 Ejemplo: "loginflow.json"
     * @return Object[][] compatible con @DataProvider, donde cada fila es
     *         un Map con los campos del objeto JSON.
     * @throws RuntimeException si el archivo no existe o tiene formato inválido.
     */
    public static Object[][] getData(String filename) {
        try (InputStream is = JsonDataProvider.class.getClassLoader()
                .getResourceAsStream("testdata/" + filename)) {

            if (is == null) {
                throw new RuntimeException("Archivo no encontrado: testdata/" + filename
                        + "\nVerifica que esté en src/test/resources/testdata/");
            }

            JsonNode root = mapper.readTree(is);
            List<Object[]> rows = new ArrayList<>();

            root.fields().forEachRemaining(entry -> {
                Map<String, String> rowData = new LinkedHashMap<>();
                entry.getValue().fields().forEachRemaining(field ->
                        rowData.put(field.getKey(), field.getValue().asText()));
                rows.add(new Object[]{rowData});
            });

            return rows.toArray(new Object[0][]);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al leer testdata/" + filename + ": " + e.getMessage(), e);
        }
    }
}
