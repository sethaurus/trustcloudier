/**
 * @author Thomas Drake-Brockman
**/

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.nio.charset.Charset;
import javax.net.ssl.SSLSocket;

public class TCSocket {
  private SSLSocket sock;
  private DataInputStream sockIn;
  private DataOutputStream sockOut;

  public TCSocket(SSLSocket sock) throws TCSocketException {
    this.sock = sock;
    try {
      this.sockOut = new DataOutputStream(sock.getOutputStream());
      this.sockIn = new DataInputStream(sock.getInputStream());
    } catch (IOException e) {
      throw new TCSocketException("Unable to open socket streams.");
    }
  }

  public void sendPacket(TCMessage packet) throws TCSocketException {
    try {
      byte[] packetBytes = packet.toBytes();

      sockOut.writeInt(packetBytes.length);
      sockOut.write(packetBytes, 0, packetBytes.length);
    } catch (IOException e) {
      throw new TCSocketException("Failed to write packet to socket.");
    }
  }

  public TCMessage readPacket() throws TCSocketException {
    try {
      int packetLength = sockIn.readInt();
      byte[] packetBytes = new byte[packetLength];

      sockIn.readFully(packetBytes);
      TCMessageFactory packetFactory = new TCMessageFactory(packetBytes);

      return packetFactory.getPacket();

    } catch (IOException e) {
      throw new TCSocketException("Failed to read packet from socket.");
    }
  }
}