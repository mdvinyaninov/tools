package iteco.devops;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;

public class HTTPClientFactory {
    protected static Logger LOGGER = LogManager.getLogger(HTTPClientFactory.class);

    public static CloseableHttpClient getClient() {
        CloseableHttpClient client = null;

        try {
            SSLContext sslContext = (new SSLContextBuilder())
                    .loadTrustMaterial(null, (x509Certificates, authType) -> true)
                    .build();

            Registry reg = RegistryBuilder
                    .create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE)).build();

            PoolingHttpClientConnectionManager m = new PoolingHttpClientConnectionManager(reg);

            client = HttpClientBuilder
                    .create()
                    .setSSLContext(sslContext)
                    .setConnectionManager(m)
                    .build();
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }

        return client;
    }
}
