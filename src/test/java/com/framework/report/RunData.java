package com.framework.report;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Payload completo que se inyecta en el template HTML.
 * Los nombres de campo deben coincidir con REPORT_DATA en report-template.html.
 */
public class RunData {
    public Meta meta = new Meta();
    public List<TestResultData> tests;

    public static class Meta {
        public String suiteName;
        public String envLabel;
        public String buildId;
        public String executedAt;
        public String framework;
        public Map<String, String> environment = new LinkedHashMap<>();
        public String version;
        public String year;
    }
}
