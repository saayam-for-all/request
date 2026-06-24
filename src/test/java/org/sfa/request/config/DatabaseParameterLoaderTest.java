package org.sfa.request.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseParameterLoaderTest {

    @AfterEach
    void clearDatasourceProperties() {
        System.clearProperty("spring.datasource.url");
        System.clearProperty("spring.datasource.username");
        System.clearProperty("spring.datasource.password");
        System.clearProperty("spring.sql.init.mode");
    }

    @Test
    void configureIfPresentLeavesLocalConfigurationWhenPrimaryPathIsMissing() {
        DatabaseParameterLoader loader = new DatabaseParameterLoader(path -> {
            throw new AssertionError("Parameter Store should not be called");
        }, Map.of());

        loader.configureIfPresent();

        assertThat(System.getProperty("spring.datasource.url")).isNull();
        assertThat(System.getProperty("spring.datasource.username")).isNull();
        assertThat(System.getProperty("spring.datasource.password")).isNull();
        assertThat(System.getProperty("spring.sql.init.mode")).isNull();
    }

    @Test
    void configureIfPresentSetsSpringDatasourceFromPrimaryParameter() {
        Map<String, String> environment = new HashMap<>();
        environment.put(DatabaseParameterLoader.PRIMARY_DB_PARAMETER_PATH, "/dev/saayam/db/Virginia/Request/user");
        environment.put(DatabaseParameterLoader.FALLBACK_DB_PARAMETER_PATH, "/dev/saayam/db/Ireland/Request/user");

        DatabaseParameterLoader loader = new DatabaseParameterLoader(path -> """
                {
                  "HOST": "request-db.example.com",
                  "USERNAME": "request_user",
                  "PASSWORD": "secret-value",
                  "DATABASE_NAME": "postgres",
                  "PORT": "5432",
                  "SSL": "true"
                }
                """, environment);

        loader.configureIfPresent();

        assertThat(System.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://request-db.example.com:5432/postgres?sslmode=require&currentSchema=virginia_dev_saayam_rdbms");
        assertThat(System.getProperty("spring.datasource.username")).isEqualTo("request_user");
        assertThat(System.getProperty("spring.datasource.password")).isEqualTo("secret-value");
        assertThat(System.getProperty("spring.sql.init.mode")).isEqualTo("never");
    }

    @Test
    void parseSecretAcceptsMixedCaseKeyNames() {
        DatabaseSecret secret = DatabaseParameterLoader.parseSecret("""
                {
                  "Host": "request-db.example.com",
                  "Username": "request_user",
                  "Password": "secret-value",
                  "Database_name": "postgres",
                  "Port": "5432",
                  "SSL": "true"
                }
                """);

        assertThat(secret.host()).isEqualTo("request-db.example.com");
        assertThat(secret.username()).isEqualTo("request_user");
        assertThat(secret.password()).isEqualTo("secret-value");
        assertThat(secret.databaseName()).isEqualTo("postgres");
        assertThat(secret.port()).isEqualTo("5432");
        assertThat(secret.ssl()).isEqualTo("true");
    }

    @Test
    void parseSecretAcceptsDatabaseNameWithHyphen() {
        DatabaseSecret secret = DatabaseParameterLoader.parseSecret("""
                {
                  "HOST": "request-db.example.com",
                  "USERNAME": "request_user",
                  "PASSWORD": "secret-value",
                  "database-name": "postgres",
                  "PORT": "5432",
                  "SSL": "true"
                }
                """);

        assertThat(secret.databaseName()).isEqualTo("postgres");
    }

    @Test
    void parseSecretAcceptsLowercaseKeyNames() {
        DatabaseSecret secret = DatabaseParameterLoader.parseSecret("""
                {
                  "host": "request-db.example.com",
                  "username": "request_user",
                  "password": "secret-value",
                  "database_name": "postgres",
                  "port": "5432",
                  "ssl": "false"
                }
                """);

        assertThat(secret.jdbcUrl())
                .isEqualTo("jdbc:postgresql://request-db.example.com:5432/postgres?sslmode=disable&currentSchema=virginia_dev_saayam_rdbms");
    }

    @Test
    void parseSecretRejectsInvalidJson() {
        assertThatThrownBy(() -> DatabaseParameterLoader.parseSecret("not-json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not valid JSON");
    }

    @Test
    void parseSecretRejectsMissingRequiredKeys() {
        String json = """
                {
                  "HOST": "request-db.example.com",
                  "USERNAME": "request_user",
                  "PASSWORD": "secret-value",
                  "DATABASE_NAME": "postgres",
                  "PORT": "5432"
                }
                """;

        assertThatThrownBy(() -> DatabaseParameterLoader.parseSecret(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SSL")
                .hasMessageContaining("Available normalized keys")
                .hasMessageContaining("HOST")
                .hasMessageContaining("DATABASE_NAME");
    }

    @Test
    void parseSecretRejectsMissingRequiredNormalizedKey() {
        String json = """
                {
                  "Host": "request-db.example.com",
                  "Username": "request_user",
                  "Password": "secret-value",
                  "Database_name": "postgres",
                  "SSL": "true"
                }
                """;

        assertThatThrownBy(() -> DatabaseParameterLoader.parseSecret(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PORT")
                .hasMessageContaining("Available normalized keys");
    }

    @Test
    void jdbcUrlConstructionNormalizesSslValue() {
        DatabaseSecret secret = secretWithSsl("TRUE");

        assertThat(secret.jdbcUrl())
                .isEqualTo("jdbc:postgresql://request-db.example.com:5432/postgres?sslmode=require&currentSchema=virginia_dev_saayam_rdbms");
    }

    @Test
    void jdbcUrlConstructionMapsFalseToDisableSslMode() {
        DatabaseSecret secret = secretWithSsl("false");

        assertThat(secret.jdbcUrl())
                .isEqualTo("jdbc:postgresql://request-db.example.com:5432/postgres?sslmode=disable&currentSchema=virginia_dev_saayam_rdbms");
    }

    @Test
    void jdbcUrlConstructionAcceptsRequireSslMode() {
        DatabaseSecret secret = secretWithSsl("require");

        assertThat(secret.jdbcUrl())
                .isEqualTo("jdbc:postgresql://request-db.example.com:5432/postgres?sslmode=require&currentSchema=virginia_dev_saayam_rdbms");
    }

    @Test
    void jdbcUrlConstructionAcceptsVerifyFullSslMode() {
        DatabaseSecret secret = secretWithSsl("verify-full");

        assertThat(secret.jdbcUrl())
                .isEqualTo("jdbc:postgresql://request-db.example.com:5432/postgres?sslmode=verify-full&currentSchema=virginia_dev_saayam_rdbms");
    }

    @Test
    void jdbcUrlConstructionMapsEnabledToRequireSslMode() {
        DatabaseSecret secret = secretWithSsl(" enabled ");

        assertThat(secret.jdbcUrl())
                .isEqualTo("jdbc:postgresql://request-db.example.com:5432/postgres?sslmode=require&currentSchema=virginia_dev_saayam_rdbms");
    }

    @Test
    void jdbcUrlConstructionMapsDisabledToDisableSslMode() {
        DatabaseSecret secret = secretWithSsl(" disabled ");

        assertThat(secret.jdbcUrl())
                .isEqualTo("jdbc:postgresql://request-db.example.com:5432/postgres?sslmode=disable&currentSchema=virginia_dev_saayam_rdbms");
    }

    @Test
    void jdbcUrlConstructionRejectsInvalidSslValue() {
        DatabaseSecret secret = secretWithSsl("sometimes");

        assertThatThrownBy(secret::jdbcUrl)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SSL");
    }

    @Test
    void jdbcUrlConstructionRejectsInvalidPort() {
        DatabaseSecret secret = new DatabaseSecret(
                "request-db.example.com",
                "request_user",
                "secret-value",
                "postgres",
                "99999",
                "true"
        );

        assertThatThrownBy(secret::jdbcUrl)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PORT");
    }

    @Test
    void configureIfPresentPropagatesSsmRetrievalFailure() {
        Map<String, String> environment = Map.of(
                DatabaseParameterLoader.PRIMARY_DB_PARAMETER_PATH,
                "/dev/saayam/db/Virginia/Request/user"
        );

        DatabaseParameterLoader loader = new DatabaseParameterLoader(path -> {
            throw new IllegalStateException("ssm unavailable");
        }, environment);

        assertThatThrownBy(loader::configureIfPresent)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ssm unavailable");
    }

    @Test
    void configureIfPresentDoesNotBlockStartupWhenFallbackSecretFails() {
        Map<String, String> environment = Map.of(
                DatabaseParameterLoader.PRIMARY_DB_PARAMETER_PATH,
                "/dev/saayam/db/Virginia/Request/user",
                DatabaseParameterLoader.FALLBACK_DB_PARAMETER_PATH,
                "/dev/saayam/db/Ireland/Request/user"
        );

        DatabaseParameterLoader loader = new DatabaseParameterLoader(path -> {
            if (path.contains("Ireland")) {
                throw new IllegalArgumentException("fallback unavailable");
            }
            return """
                    {
                      "Host": "request-db.example.com",
                      "Username": "request_user",
                      "Password": "secret-value",
                      "Database_name": "postgres",
                      "Port": "5432",
                      "SSL": "true"
                    }
                    """;
        }, environment);

        loader.configureIfPresent();

        assertThat(System.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://request-db.example.com:5432/postgres?sslmode=require&currentSchema=virginia_dev_saayam_rdbms");
        assertThat(System.getProperty("spring.datasource.username")).isEqualTo("request_user");
        assertThat(System.getProperty("spring.datasource.password")).isEqualTo("secret-value");
        assertThat(System.getProperty("spring.sql.init.mode")).isEqualTo("never");
    }

    private static DatabaseSecret secretWithSsl(String ssl) {
        return new DatabaseSecret(
                "request-db.example.com",
                "request_user",
                "secret-value",
                "postgres",
                "5432",
                ssl
        );
    }
}
