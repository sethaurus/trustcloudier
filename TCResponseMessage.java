/**
 * @author Thomas Drake-Brockman
**/

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

abstract class TCResponseMessage implements TCMessage {
  private boolean success;
  public String message;

  public TCResponseMessage(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public TCResponseMessage(byte[] packetBytes) {
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
  }

  public byte[] toBytes() {
    byte[] messageBytes = message.getBytes(Charset.forName("UTF-8"));

    // Packet Type - 1 byte
    // Success - 1 byte
    // Message Length - 4 bytes
    // Message - variable
    int packetLength = 1 + 1 + 4 + messageBytes.length;
    ByteBuffer buff = ByteBuffer.allocate(packetLength);

    buff.put((byte) getType());
    buff.put((byte) (success ? 1 : 0));
    buff.putInt(messageBytes.length);
    buff.put(messageBytes);

    return buff.array();
  }

  abstract int getType();
}