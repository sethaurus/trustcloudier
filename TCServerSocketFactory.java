/**
 * @author Thomas Drake-Brockman
**/

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.KeyManagerFactory;

public class TCServerSocketFactory {
  private int port;

  public TCServerSocketFactory(int port) {
    this.port = port;
  }

  public TCServerSocket open() throws TCSocketException {  
    try {
      return new TCServerSocket((SSLServerSocket) getSSLContext().getServerSocketFactory().createServerSocket(this.port));
    } catch (IOException e) {
      throw new TCSocketException(String.format("Unable to bind on port %d.", this.port));
    }
  }

  private SSLContext getSSLContext() throws TCSocketException {
    SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance("SSLv3");
    } catch (NoSuchAlgorithmException e) {
      throw new TCSocketException("Unable to use SSLv3. Exiting.");    
    }

    try {
      sslContext.init(getKeyManagerFactory().getKeyManagers(), null, null);
    } catch (KeyManagementException e) {
      throw new TCSocketException("Unable to load server key. Exiting.");
    }

    return sslContext;
  }

  private KeyManagerFactory getKeyManagerFactory() throws TCSocketException {
    KeyManagerFactory keyManagerFactory;
    try {
      keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
      keyManagerFactory.init(getKeyStore(), "trust".toCharArray());
    } catch (NoSuchAlgorithmException e) {
      throw new TCSocketException("Unable to initialize key manager. Exiting.");
    } catch (KeyStoreException | UnrecoverableKeyException e) {
      throw new TCSocketException("Unable to load key store into key manager. Exiting.");
    }
    return keyManagerFactory;
  }

  private KeyStore getKeyStore() throws TCSocketException {
    KeyStore keyStore;
    try {
      keyStore = KeyStore.getInstance("PKCS12");
      keyStore.load(getClass().getResourceAsStream("server_key.p12"), "trust".toCharArray());
    } catch (KeyStoreException | NoSuchAlgorithmException e) {
      throw new TCSocketException("Unable to initialize key store. Exiting.");
    } catch (IOException | CertificateException e) {
      throw new TCSocketException("Unable to load server key. Exiting.");
    }
    return keyStore;
  }
}