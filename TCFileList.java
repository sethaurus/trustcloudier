/**
 * @author Thomas Drake-Brockman
**/

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

class TCFileList {
  private List<Entry> items;

  public TCFileList() {
    items = new ArrayList<Entry>();
  }

  public TCFileList(byte[] bytes) {
    items = new ArrayList<Entry>();

    ByteBuffer buff = ByteBuffer.wrap(bytes);

    int numItems = buff.getInt();

    for (int i = 0; i < numItems; i++) {
      int entryBytesLength = buff.getInt();

      byte[] entryBytes = new byte[entryBytesLength];
      buff.get(entryBytes);

      items.add(new Entry(entryBytes));
    }
  }

  public void add(String fileName, int protection, String[] signatures) {
    items.add(new Entry(fileName, protection, signatures));
  }

  public List<Entry> getItems() {
    return items;
  }

  public byte[] toBytes() {
    byte[][] itemsBytes = new byte[items.size()][];
    int itemsBytesLength = 0;

    for (int i = 0; i < items.size(); i++) {
      itemsBytes[i] = items.get(i).toBytes();
      itemsBytesLength += itemsBytes[i].length;
    }

    int packetLength = 4 + 4 * itemsBytes.length + itemsBytesLength;
    ByteBuffer buff = ByteBuffer.allocate(packetLength);

    buff.putInt(itemsBytes.length);
    for (byte[] itemBytes : itemsBytes) {
      buff.putInt(itemBytes.length);
      buff.put(itemBytes);
    }

    return buff.array();
  }

  private class Entry {
    public String fileName;
    public int protection;
    public String[] signatures;

    public Entry(String fileName, int protection, String[] signatures) {
      this.fileName = fileName;
      this.protection = protection;
      this.signatures = signatures;
    }

    public Entry(byte[] bytes) {
      ByteBuffer buff = ByteBuffer.wrap(bytes);

      int fileNameLength = buff.getInt();

      byte[] fileNameBytes = new byte[fileNameLength];
      buff.get(fileNameBytes);

      try {
        this.fileName = new String(fileNameBytes, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        System.exit(1);
      }

      this.protection = buff.getInt();

      int numSignatures = buff.getInt();
      this.signatures = new String[numSignatures];

      for (int i = 0; i < numSignatures; i++) {
        int sigBytesLength = buff.getInt();

        byte[] sigBytes = new byte[sigBytesLength];
        buff.get(sigBytes);

        try {
          this.signatures[i] = new String(sigBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          System.exit(1);
        }
      }

    }

    public byte[] toBytes() {
      byte[] fileNameBytes = fileName.getBytes(Charset.forName("UTF-8"));

      byte[][] signaturesBytes = new byte[signatures.length][];
      int signaturesBytesLength = 0;

      for (int i = 0; i < signatures.length; i++) {
        signaturesBytes[i] = signatures[i].getBytes(Charset.forName("UTF-8"));
        signaturesBytesLength += signaturesBytes[i].length;
      }

      int packetLength = 4 + fileNameBytes.length + 4  + 4 + 4 * signaturesBytes.length + signaturesBytesLength;
      ByteBuffer buff = ByteBuffer.allocate(packetLength);

      buff.putInt(fileNameBytes.length);
      buff.put(fileNameBytes);

      buff.putInt(protection);

      buff.putInt(signaturesBytes.length);
      for (byte[] signatureBytes : signaturesBytes) {
        buff.putInt(signatureBytes.length);
        buff.put(signatureBytes);
      }

      return buff.array();
    }
  }
}