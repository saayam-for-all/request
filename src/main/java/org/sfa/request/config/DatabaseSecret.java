package org.sfa.request.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public record DatabaseSecret(
        String host,
        String username,
        String password,
        String databaseName,
        String port,
        String ssl
) {

    private static final String VIRGINIA_SCHEMA = "virginia_dev_saayam_rdbms";
    private static final Set<String> VALID_SSL_MODES = Set.of(
            "disable",
            "allow",
            "prefer",
            "require",
            "verify-ca",
            "verify-full"
    );

    public String jdbcUrl() {
        int parsedPort = parsePort(port);
        String sslMode = normalizeSslMode(ssl);

        return "jdbc:postgresql://" + host.trim() + ":" + parsedPort + "/" + databaseName.trim()
                + "?sslmode=" + encodeQueryValue(sslMode)
                + "&currentSchema=" + encodeQueryValue(VIRGINIA_SCHEMA);
    }

    private static String normalizeSslMode(String ssl) {
        String normalizedSsl = ssl.trim().toLowerCase(Locale.ROOT);
        if (normalizedSsl.equals("true")
                || normalizedSsl.equals("yes")
                || normalizedSsl.equals("1")
                || normalizedSsl.equals("enabled")) {
            return "require";
        }

        if (normalizedSsl.equals("false")
                || normalizedSsl.equals("no")
                || normalizedSsl.equals("0")
                || normalizedSsl.equals("disabled")) {
            return "disable";
        }

        if (VALID_SSL_MODES.contains(normalizedSsl)) {
            return normalizedSsl;
        }

        throw new IllegalArgumentException("Database parameter SSL must be a boolean-like value or valid PostgreSQL sslmode");
    }

    private static int parsePort(String port) {
        try {
            int parsed = Integer.parseInt(port.trim());
            if (parsed < 1 || parsed > 65535) {
                throw new IllegalArgumentException("Database parameter PORT must be between 1 and 65535");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Database parameter PORT must be numeric", e);
        }
    }

    private static String encodeQueryValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
