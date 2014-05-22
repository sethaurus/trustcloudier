/**
 * @author Thomas Drake-Brockman
**/

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class TCListResponseMessage implements TCMessage {
  private TCFileList files;

  public TCListResponseMessage(TCFileList files) {
    this.files = files;
  }

  public TCListResponseMessage(byte[] packetBytes) {
    ByteBuffer buff = ByteBuffer.wrap(packetBytes);

    // Discard the command type byte
    buff.get();

    int fileListBytesLength = buff.getInt();

    byte[] fileListBytes = new byte[fileListBytesLength];
    buff.get(fileListBytes);

    this.files = new TCFileList(fileListBytes);
  }

  public byte[] toBytes() {
    byte[] fileListBytes = files.toBytes();

    // Packet Type - 1 byte
    // Message Length - 4 bytes
    // Message - variable
    int packetLength = 1 + 4 + fileListBytes.length;
    ByteBuffer buff = ByteBuffer.allocate(packetLength);

    buff.put((byte) 7);
    buff.putInt(fileListBytes.length);
    buff.put(fileListBytes);
    
    return buff.array();
  }
}