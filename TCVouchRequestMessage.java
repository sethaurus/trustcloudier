/**
 * @author Thomas Drake-Brockman
**/

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class TCVouchRequestMessage implements TCMessage {
  public String fileName;
  public String certName;
  public byte[] signatureData;

  public TCVouchRequestMessage(String fileName, String certName, byte[] signatureData) {
    this.fileName = fileName;
    this.certName = certName;
    this.signatureData = signatureData;
  }

  public TCVouchRequestMessage(byte[] packetBytes) {
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

    int certNameLength = buff.getInt();

    byte[] certNameBytes = new byte[certNameLength];
    buff.get(certNameBytes);

    try {
      this.certName = new String(certNameBytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      System.exit(1);
    }

    int signatureDataLength = buff.getInt();

    this.signatureData = new byte[signatureDataLength];
    buff.get(this.signatureData);
  }

  public byte[] toBytes() {
    byte[] fileNameBytes = fileName.getBytes(Charset.forName("UTF-8"));
    byte[] certNameBytes = certName.getBytes(Charset.forName("UTF-8"));

    // Packet Type - 1 byte
    // Filename Length - 4 bytes
    // Filename - variable
    // Certname Length - 4 bytes
    // Certname - variable
    // Signature Length - 4 bytes
    // Signature - variable
    int packetLength = 1 + 4 + fileNameBytes.length + 4 + certNameBytes.length + 4 + signatureData.length;

    ByteBuffer buff = ByteBuffer.allocate(packetLength);

    buff.put((byte) 2);
    buff.putInt(fileNameBytes.length);
    buff.put(fileNameBytes);
    buff.putInt(certNameBytes.length);
    buff.put(certNameBytes);
    buff.putInt(signatureData.length);
    buff.put(signatureData);

    return buff.array();
  }
}