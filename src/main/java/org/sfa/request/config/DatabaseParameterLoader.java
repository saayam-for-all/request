package org.sfa.request.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class DatabaseParameterLoader {

    public static final String PRIMARY_DB_PARAMETER_PATH = "PRIMARY_DB_PARAMETER_PATH";
    public static final String FALLBACK_DB_PARAMETER_PATH = "FALLBACK_DB_PARAMETER_PATH";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ParameterStoreClient parameterStoreClient;
    private final Map<String, String> environment;

    public DatabaseParameterLoader(ParameterStoreClient parameterStoreClient, Map<String, String> environment) {
        this.parameterStoreClient = Objects.requireNonNull(parameterStoreClient, "parameterStoreClient is required");
        this.environment = Objects.requireNonNull(environment, "environment is required");
    }

    public static void configureSpringDatasourceIfPresent() {
        new DatabaseParameterLoader(new SsmParameterStoreClient(), System.getenv()).configureIfPresent();
    }

    public void configureIfPresent() {
        String primaryPath = environment.get(PRIMARY_DB_PARAMETER_PATH);
        if (isBlank(primaryPath)) {
            log.info("PRIMARY_DB_PARAMETER_PATH not set; using datasource configuration from Spring properties");
            return;
        }

        DatabaseSecret primarySecret = loadSecret(primaryPath, "primary");
        applyPrimaryDatasource(primarySecret);

        String fallbackPath = environment.get(FALLBACK_DB_PARAMETER_PATH);
        if (!isBlank(fallbackPath)) {
            try {
                loadSecret(fallbackPath, "fallback");
                log.info("Fallback database configuration loaded from Parameter Store; automatic failover is not enabled yet");
            } catch (RuntimeException e) {
                log.warn("Unable to load fallback database configuration from Parameter Store path {}; continuing with primary database configuration. Cause: {}",
                        fallbackPath, e.getClass().getSimpleName());
            }
        }
    }

    private DatabaseSecret loadSecret(String parameterPath, String label) {
        try {
            String parameterValue = parameterStoreClient.getSecureParameter(parameterPath);
            DatabaseSecret secret = parseSecret(parameterValue);
            log.info("Loaded {} database configuration from Parameter Store path {}", label, parameterPath);
            return secret;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load " + label + " database configuration from Parameter Store", e);
        }
    }

    private void applyPrimaryDatasource(DatabaseSecret secret) {
        System.setProperty("spring.datasource.url", secret.jdbcUrl());
        System.setProperty("spring.datasource.username", secret.username());
        System.setProperty("spring.datasource.password", secret.password());
        System.setProperty("spring.sql.init.mode", "never");
        log.info("Configured primary datasource from Parameter Store and disabled SQL script initialization");
    }

    public static DatabaseSecret parseSecret(String json) {
        if (isBlank(json)) {
            throw new IllegalArgumentException("Database parameter value is empty");
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            Map<String, JsonNode> normalizedFields = normalizeFields(root);
            String host = requiredText(normalizedFields, "HOST");
            String username = requiredText(normalizedFields, "USERNAME");
            String password = requiredText(normalizedFields, "PASSWORD");
            String databaseName = requiredText(normalizedFields, "DATABASE_NAME");
            String port = requiredText(normalizedFields, "PORT");
            String ssl = requiredText(normalizedFields, "SSL");

            return new DatabaseSecret(host, username, password, databaseName, port, ssl);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Database parameter value is not valid JSON", e);
        }
    }

    private static Map<String, JsonNode> normalizeFields(JsonNode root) {
        Map<String, JsonNode> normalizedFields = new LinkedHashMap<>();
        if (!root.isObject()) {
            logAvailableKeys(normalizedFields.keySet());
            throw new IllegalArgumentException("Database parameter value must be a JSON object");
        }

        root.fields().forEachRemaining(entry ->
                normalizedFields.put(normalizeKey(entry.getKey()), entry.getValue()));
        return normalizedFields;
    }

    private static String requiredText(Map<String, JsonNode> normalizedFields, String key) {
        JsonNode value = normalizedFields.get(key);
        if (value == null || value.isNull() || isBlank(value.asText())) {
            logAvailableKeys(normalizedFields.keySet());
            throw new IllegalArgumentException("Database parameter is missing required key: " + key
                    + ". Available normalized keys: " + normalizedFields.keySet());
        }
        return value.asText().trim();
    }

    private static String normalizeKey(String key) {
        return key.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');
    }

    private static void logAvailableKeys(Set<String> keys) {
        log.warn("Database parameter contains keys: {}", keys);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
