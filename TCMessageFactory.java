/**
 * @author Thomas Drake-Brockman
**/

public class TCMessageFactory {
  private byte[] packetBytes;

  public TCMessageFactory(byte[] packetBytes) {
    this.packetBytes = packetBytes;
  }

  public TCMessage getPacket() {
    byte packetType = packetBytes[0];

    switch (packetType) {
      case 0:
        return new TCUploadRequestMessage(packetBytes);
      case 1:
        return new TCDownloadRequestMessage(packetBytes);
      case 2:
        return new TCVouchRequestMessage(packetBytes);
      case 3:
        return new TCListRequestMessage(packetBytes);
      case 4:
        return new TCUploadResponseMessage(packetBytes);  
      case 5:
        return new TCVouchResponseMessage(packetBytes);
      case 6:
        return new TCDownloadResponseMessage(packetBytes);
      case 7:
        return new TCListResponseMessage(packetBytes);
      default:
        return null;
    }
  }
}