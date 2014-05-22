/**
 * @author Thomas Drake-Brockman
**/

class TCVouchResponseMessage extends TCResponseMessage {
  public TCVouchResponseMessage(boolean success, String message) {
    super(success, message);
  }

  public TCVouchResponseMessage(byte[] packetBytes) {
    super(packetBytes);
  }

  int getType() {
    return 5;
  }
}