/**
 * @author Thomas Drake-Brockman
**/

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class TCDownloadRequestMessage implements TCMessage {
  public String fileName;
  public int protection;

  public TCDownloadRequestMessage(String fileName, int protection) {
    this.fileName = fileName;
    this.protection = protection;
  }

  public TCDownloadRequestMessage(byte[] packetBytes) {
    ByteBuffer buff = ByteBuffer.wrap(packetBytes);

    // Discard the command type byte
    buff.get();

    int fileNameLength = buff.getInt();

    byte[] fileNameBytes = new byte[fileNameLength];
    buff.get(fileNameBytes);

    try {
      this.fileName = new String(fileNameBytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      System.exit(1);
    }

    this.protection = buff.getInt();
  }

  public byte[] toBytes() {
    byte[] fileNameBytes = fileName.getBytes(Charset.forName("UTF-8"));

    // Packet Type - 1 byte
    // Filename Length - 4 bytes
    // Filename - variable
    // Protection - 4 bytes
    int packetLength = 1 + 4 + fileNameBytes.length + 4;
    ByteBuffer buff = ByteBuffer.allocate(packetLength);


    buff.put((byte) 1);
    buff.putInt(fileNameBytes.length);
    buff.put(fileNameBytes);
    buff.putInt(protection);

    return buff.array();
  }
}