/**
 * @author Thomas Drake-Brockman
**/

import java.nio.ByteBuffer;

class TCListRequestMessage implements TCMessage {
  public TCListRequestMessage() {
    return;
  }

  public TCListRequestMessage(byte[] packetBytes) {
    return;
  }

  public byte[] toBytes() {
    // Packet Type - 1 byte
    ByteBuffer buff = ByteBuffer.allocate(1);

    buff.put((byte) 3);

    return buff.array();
  }
}