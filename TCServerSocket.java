/**
 * @author Thomas Drake-Brockman
**/

import java.io.IOException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

class TCServerSocket {
  private SSLServerSocket serverSock;

  public TCServerSocket(SSLServerSocket serverSock) {
    this.serverSock = serverSock;
  }

  public TCSocket accept() throws TCSocketException {
    try {
      return new TCSocket((SSLSocket) serverSock.accept());
    } catch (IOException e) {
      throw new TCSocketException("Failed to write message to socket.");
    }
  }
}