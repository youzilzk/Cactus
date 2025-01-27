package com.youzi.tunnel.client.work;

import android.content.res.AssetManager;
import com.youzi.tunnel.client.config.ClientProperties;
import com.youzi.tunnel.client.manager.Manager;
import com.youzi.tunnel.common.utils.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class SslContextCreator {
    private static final LoggerFactory log = LoggerFactory.getLogger();
    private static SSLContext sslContext;
    private static KeyStore keyStore;

    public static SSLContext createSSLContext() {
        return initSSLContext();
    }

    public static SSLContext getSSLContext() {
        if (sslContext == null) {
            sslContext = initSSLContext();
        }
        return sslContext;
    }

    private static SSLContext initSSLContext() {
        log.info("Checking SSL configuration properties...");
        final String jksPath = ClientProperties.getInstance().getSslJksPath();
        if (jksPath == null || jksPath.isEmpty()) {
            log.info("The keystore path is null or empty. The SSL context won't be initialized.");
            return null;
        }
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            if (keyStore == null) {
                AssetManager manager = Manager.getAssetManager();
                initKeyStore(manager, jksPath);
            }
            tmf.init(keyStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            log.info("Initializing SSL context...");

            SSLContext clientSSLContext = SSLContext.getInstance("TLS");
            clientSSLContext.init(null, trustManagers, null);
            log.info("The SSL context has been initialized successfully.");

            return clientSSLContext;
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | CertificateException | IOException ex) {
            log.info("Unable to initialize SSL context. Cause = {}, errorMessage = {}.", ex.getCause(), ex.getMessage());
            return null;
        }
    }

    private static void initKeyStore(AssetManager manager, String jksPath) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        log.info("read {}  and Initializing KeyStore...", jksPath);
        InputStream jksInputStream = manager.open(jksPath);
        final String keyStorePassword = ClientProperties.getInstance().getSslKeyStorePassword();
        final KeyStore ks = KeyStore.getInstance("BKS");
        ks.load(jksInputStream, keyStorePassword.toCharArray());
        keyStore = ks;
    }

}