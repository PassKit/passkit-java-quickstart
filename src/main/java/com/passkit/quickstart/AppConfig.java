package com.passkit.quickstart;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** Loads configuration from environment variables, .env, then passkit.properties. */
public final class AppConfig {
    private final Properties properties = new Properties();
    private final Map<String, String> envFile = new LinkedHashMap<>();

    private AppConfig() throws IOException {
        try (InputStream input = AppConfig.class.getResourceAsStream("/passkit.properties")) {
            if (input != null) properties.load(input);
        }
        Path path = Path.of(".env");
        if (Files.isRegularFile(path)) {
            for (String rawLine : Files.readAllLines(path)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                int separator = line.indexOf('=');
                String value = line.substring(separator + 1).trim();
                if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                envFile.put(line.substring(0, separator).trim(), value);
            }
        }
    }

    public static AppConfig load() throws IOException {
        return new AppConfig();
    }

    public String get(String envName, String propertyName, String defaultValue) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) value = envFile.get(envName);
        if (value == null || value.isBlank()) value = properties.getProperty(propertyName);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    public int getInt(String envName, String propertyName, int defaultValue) {
        return Integer.parseInt(get(envName, propertyName, Integer.toString(defaultValue)));
    }

    public String host() { return get("PASSKIT_GRPC_HOST", "grpc.host", "grpc.pub1.passkit.io"); }
    public int port() { return getInt("PASSKIT_GRPC_PORT", "grpc.port", 443); }
    public String certificate() { return get("PASSKIT_CERTIFICATE", "credentials.certificate", "src/main/resources/credentials/certificate.pem"); }
    public String key() { return get("PASSKIT_KEY", "credentials.key", "src/main/resources/credentials/key-java.pem"); }
    public String caChain() { return get("PASSKIT_CA_CHAIN", "credentials.chain", "src/main/resources/credentials/ca-chain.pem"); }
    public String keyPassword() { return get("PASSKIT_KEY_PASSWORD", "credentials.password", ""); }
    public String baseEmail() { return get("PASSKIT_BASE_EMAIL", "credentials.baseEmail", ""); }
    public String vipEmail() { return get("PASSKIT_VIP_EMAIL", "credentials.vipEmail", baseEmail()); }
    public String appleCertificateId() { return get("PASSKIT_APPLE_CERTIFICATE_ID", "credentials.appleCertificate", ""); }
    public int poolSize() { return Math.max(1, getInt("PASSKIT_POOL_SIZE", "grpc.pool.size", 4)); }
    public int cleanupDelaySeconds() { return getInt("PASSKIT_CLEANUP_DELAY_SECONDS", "delete.assets.timeout.seconds", 0); }
}
