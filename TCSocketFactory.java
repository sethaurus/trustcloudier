/**
 * @author Thomas Drake-Brockman
**/

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class TCSocketFactory {
  private String host;
  private int port;

  public TCSocketFactory(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public TCSocket open() throws TCSocketException {
    try {
      return new TCSocket((SSLSocket) getSSLContext().getSocketFactory().createSocket(this.host, this.port));
    } catch (IOException e) {
      throw new TCSocketException(String.format("Unable to connect to %s on port %d.", this.host, this.port));
    }
  }

  private SSLContext getSSLContext() throws TCSocketException {
    SSLContext sslContext;

    try {
      sslContext = SSLContext.getInstance("SSLv3");
    } catch (NoSuchAlgorithmException e) {
      throw new TCSocketException("Unable to use SSLv3.");   
    }

    try {
      sslContext.init(null, getTrustManagerFactory().getTrustManagers(), null);
    } catch (KeyManagementException e) {
      throw new TCSocketException("Unable to load server key.");
    }

    return sslContext;
  }

  private TrustManagerFactory getTrustManagerFactory() throws TCSocketException {
    TrustManagerFactory trustManagerFactory;

    try {
      trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
      trustManagerFactory.init(getKeyStore());
    } catch (NoSuchAlgorithmException e) {
      throw new TCSocketException("Unable to initialize trust manager.");
    } catch (KeyStoreException e) {
      throw new TCSocketException("Unable to load key store into trust manager.");
    }

    return trustManagerFactory;
  }

  private KeyStore getKeyStore() throws TCSocketException {
    KeyStore keyStore;

    try {
      keyStore = KeyStore.getInstance("JKS");
      keyStore.load(null, "".toCharArray());

      InputStream caStream = getClass().getResourceAsStream("trustcloud_ca_cert.crt");
      java.security.cert.Certificate caCert = CertificateFactory.getInstance("X.509").generateCertificate(caStream);

      // Add the certificate
      keyStore.setCertificateEntry("trustcloud", caCert);
    } catch (KeyStoreException | NoSuchAlgorithmException e) {
      throw new TCSocketException("Unable to initialize key store.");
    } catch (IOException | CertificateException e) {
      throw new TCSocketException("Unable to load Trustcolour CA certificate.");
    }

    return keyStore;
  }
}