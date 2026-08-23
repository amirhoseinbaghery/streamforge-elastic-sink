package ir.datapie.connect.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Objects;

/** Creates one reusable official Java client per SinkTask. */
public final class ElasticsearchClientFactory implements AutoCloseable {
    private final RestClient lowLevel;
    private final ElasticsearchTransport transport;
    private final ElasticsearchClient client;

    private ElasticsearchClientFactory(RestClient lowLevel, ElasticsearchTransport transport, ElasticsearchClient client) {
        this.lowLevel = lowLevel;
        this.transport = transport;
        this.client = client;
    }

    public static ElasticsearchClientFactory create(DataPieElasticsearchSinkConfig config) throws Exception {
        RestClientBuilder builder = RestClient.builder(HttpHost.create(config.string(DataPieElasticsearchSinkConfig.ES_URL)))
                .setDefaultHeaders(new org.apache.http.Header[]{
                        new BasicHeader("Accept", "application/vnd.elasticsearch+json; compatible-with=9"),
                        new BasicHeader("Content-Type", "application/vnd.elasticsearch+json; compatible-with=9")
                });

        String username = externalValue(config, DataPieElasticsearchSinkConfig.ES_USERNAME_FILE,
                config.optional(DataPieElasticsearchSinkConfig.ES_USERNAME));
        String password = externalValue(config, DataPieElasticsearchSinkConfig.ES_PASSWORD_FILE,
                config.password(DataPieElasticsearchSinkConfig.ES_PASSWORD) == null
                        ? "" : config.password(DataPieElasticsearchSinkConfig.ES_PASSWORD).value());
        if (!username.isBlank()) {
            CredentialsProvider credentials = new BasicCredentialsProvider();
            credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(http -> configureHttp(http, credentials, config.optional(DataPieElasticsearchSinkConfig.ES_CA_CERT_PATH)));
        } else if (!config.optional(DataPieElasticsearchSinkConfig.ES_CA_CERT_PATH).isBlank()) {
            builder.setHttpClientConfigCallback(http -> configureHttp(http, null, config.optional(DataPieElasticsearchSinkConfig.ES_CA_CERT_PATH)));
        }

        RestClient lowLevel = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(lowLevel, new JacksonJsonpMapper());
        return new ElasticsearchClientFactory(lowLevel, transport, new ElasticsearchClient(transport));
    }

    private static String externalValue(DataPieElasticsearchSinkConfig config, String fileKey, String fallback)
            throws Exception {
        String path = config.optional(fileKey);
        if (path.isBlank()) return fallback;
        return Files.readString(Path.of(path), StandardCharsets.UTF_8).trim();
    }

    private static HttpAsyncClientBuilder configureHttp(HttpAsyncClientBuilder http, CredentialsProvider credentials, String caPath) {
        if (credentials != null) http.setDefaultCredentialsProvider(credentials);
        try {
            SSLContext ssl = caPath == null || caPath.isBlank() ? SSLContexts.createSystemDefault() : pemTrustContext(caPath);
            http.setSSLContext(ssl).setSSLHostnameVerifier(new DefaultHostnameVerifier());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to initialize Elasticsearch TLS trust", e);
        }
        return http;
    }

    private static SSLContext pemTrustContext(String path) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate certificate;
        try (FileInputStream input = new FileInputStream(new File(path))) {
            certificate = factory.generateCertificate(input);
        }
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, null);
        store.setCertificateEntry("datapie-ca", certificate);
        TrustManagerFactory trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trust.init(store);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trust.getTrustManagers(), null);
        return context;
    }

    public ElasticsearchClient client() { return client; }

    @Override
    public void close() throws Exception {
        try {
            transport.close();
        } finally {
            lowLevel.close();
        }
    }
}
