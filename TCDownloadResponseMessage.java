/**
 * @author Thomas Drake-Brockman
**/

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class TCDownloadResponseMessage implements TCMessage {
  private boolean success;
  private String message;
  private byte[] payload;

  public TCDownloadResponseMessage(boolean success, String message, byte[] payload) {
    this.success = success;
    this.message = message;
    this.payload = payload;
  }

  public TCDownloadResponseMessage(byte[] packetBytes) {
    ByteBuffer buff = ByteBuffer.wrap(packetBytes);

    // Discard the command type byte
    buff.get();

    this.success = (buff.get() == 1);

    int messageBytesLength = buff.getInt();

    byte[] messageBytes = new byte[messageBytesLength];
    buff.get(messageBytes);

    try {
      this.message = new String(messageBytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      System.exit(1);
    }

    int payloadLength = buff.getInt();

    this.payload = new byte[payloadLength];
    buff.get(this.payload);
  }

  public byte[] toBytes() {
    byte[] messageBytes = message.getBytes(Charset.forName("UTF-8"));

    // Packet Type - 1 byte
    // Success - 1 byte
    // Message Length - 4 bytes
    // Message - variable
    // Payload Length - 4 bytes
    // Payload - variable
    int packetLength = 1 + 1 + 4 + messageBytes.length + 4 + payload.length;
    ByteBuffer buff = ByteBuffer.allocate(packetLength);

    buff.put((byte) 6);
    buff.put((byte) (success ? 1 : 0));
    buff.putInt(messageBytes.length);
    buff.put(messageBytes);
    buff.putInt(payload.length);
    buff.put(payload);

    return buff.array();
  }
}