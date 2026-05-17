package com.framework.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.commonaction.CommonActions;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Genera reports/report.html inyectando los datos serializados a JSON
 * dentro de la plantilla HTML cargada del classpath.
 */
public class ReportRenderer {

    private static final Logger log = CommonActions.getLogger();

    private static final String TEMPLATE_RESOURCE = "report-template.html";
    private static final String LOGO_RESOURCE     = "assets/logo_ravilesx.png";

    private static final String PLACEHOLDER_START = "/*__REPORT_DATA__*/";
    private static final String PLACEHOLDER_END   = "/*__REPORT_DATA_END__*/";

    private static final Path OUTPUT_HTML  = Paths.get("reports", "report.html");
    private static final Path OUTPUT_LOGO  = Paths.get("reports", "assets", "logo_ravilesx.png");

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Renderiza el reporte completo a reports/report.html y copia los assets.
     *
     * @param data datos de la suite ejecutada
     */
    public void render(RunData data) {
        try {
            String template = loadTemplate();
            String json     = mapper.writeValueAsString(data);
            String html     = injectData(template, json);

            Files.createDirectories(OUTPUT_HTML.getParent());
            Files.write(OUTPUT_HTML, html.getBytes(StandardCharsets.UTF_8));

            copyLogo();

            log.info("Reporte HTML generado: " + OUTPUT_HTML.toAbsolutePath());
        } catch (IOException e) {
            log.error("Error al generar el reporte HTML: " + e.getMessage(), e);
        }
    }

    private String loadTemplate() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(TEMPLATE_RESOURCE)) {
            if (is == null) {
                throw new IOException("Template no encontrado en classpath: " + TEMPLATE_RESOURCE);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String injectData(String template, String json) {
        int start = template.indexOf(PLACEHOLDER_START);
        int end   = template.indexOf(PLACEHOLDER_END);
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalStateException(
                    "Marcadores de datos no encontrados en el template. "
                    + "Esperados: " + PLACEHOLDER_START + " ... " + PLACEHOLDER_END);
        }
        return template.substring(0, start + PLACEHOLDER_START.length())
             + json
             + template.substring(end);
    }

    private void copyLogo() throws IOException {
        try (InputStream logo = getClass().getClassLoader().getResourceAsStream(LOGO_RESOURCE)) {
            if (logo == null) {
                log.warn("Logo no encontrado en classpath: " + LOGO_RESOURCE);
                return;
            }
            Files.createDirectories(OUTPUT_LOGO.getParent());
            Files.copy(logo, OUTPUT_LOGO, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
