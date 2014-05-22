/**
 * @author Thomas Drake-Brockman
**/

class TCUploadResponseMessage extends TCResponseMessage {
  public TCUploadResponseMessage(boolean success, String message) {
    super(success, message);
  }

  public TCUploadResponseMessage(byte[] packetBytes) {
    super(packetBytes);
  }

  int getType() {
    return 4;
  }
}