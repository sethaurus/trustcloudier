/**
 * @author Thomas Drake-Brockman
**/

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class TCUploadRequestMessage implements TCMessage {
  private String fileName;
  private byte[] fileData;

  public TCUploadRequestMessage(String fileName, byte[] fileData) {
    this.fileName = fileName;
    this.fileData = fileData;
  }

  public TCUploadRequestMessage(byte[] packetBytes) {
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

    int fileDataLength = buff.getInt();

    this.fileData = new byte[fileDataLength];
    buff.get(this.fileData);
  }

  public byte[] toBytes() {
    byte[] fileNameBytes = fileName.getBytes(Charset.forName("UTF-8"));

    // Packet Type - 1 byte
    // Filename Length - 4 bytes
    // Filename - variable
    // File Length - 4 bytes
    // File - variable
    int packetLength = 1 + 4 + fileNameBytes.length + 4 + fileData.length;
    ByteBuffer buff = ByteBuffer.allocate(packetLength);


    buff.put((byte) 0);
    buff.putInt(fileNameBytes.length);
    buff.put(fileNameBytes);
    buff.putInt(fileData.length);
    buff.put(fileData);

    return buff.array();
  }
}