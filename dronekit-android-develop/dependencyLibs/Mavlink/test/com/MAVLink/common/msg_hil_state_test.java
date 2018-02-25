/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */
         
// MESSAGE HIL_STATE PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Parser;
import com.MAVLink.ardupilotmega.CRC;
import java.nio.ByteBuffer;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
* DEPRECATED PACKET! Suffers from missing airspeed fields and singularities due to Euler angles. Please use HIL_STATE_QUATERNION instead. Sent from simulation to autopilot. This packet is useful for high throughput applications such as hardware in the loop simulations.
*/
public class msg_hil_state_test{

public static final int MAVLINK_MSG_ID_HIL_STATE = 90;
public static final int MAVLINK_MSG_LENGTH = 56;
private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_STATE;

private Parser parser = new Parser();

public CRC generateCRC(byte[] packet){
    CRC crc = new CRC();
    for (int i = 1; i < packet.length - 2; i++) {
        crc.update_checksum(packet[i] & 0xFF);
    }
    crc.finish_checksum(MAVLINK_MSG_ID_HIL_STATE);
    return crc;
}

public byte[] generateTestPacket(){
    ByteBuffer payload = ByteBuffer.allocate(6 + MAVLINK_MSG_LENGTH + 2);
    payload.put((byte)MAVLinkPacket.MAVLINK_STX); //stx
    payload.put((byte)MAVLINK_MSG_LENGTH); //len
    payload.put((byte)0); //seq
    payload.put((byte)255); //sysid
    payload.put((byte)190); //comp id
    payload.put((byte)MAVLINK_MSG_ID_HIL_STATE); //msg id
    payload.putLong((long)93372036854775807L); //time_usec
    payload.putFloat((float)73.0); //roll
    payload.putFloat((float)101.0); //pitch
    payload.putFloat((float)129.0); //yaw
    payload.putFloat((float)157.0); //rollspeed
    payload.putFloat((float)185.0); //pitchspeed
    payload.putFloat((float)213.0); //yawspeed
    payload.putInt((int)963499128); //lat
    payload.putInt((int)963499336); //lon
    payload.putInt((int)963499544); //alt
    payload.putShort((short)19523); //vx
    payload.putShort((short)19627); //vy
    payload.putShort((short)19731); //vz
    payload.putShort((short)19835); //xacc
    payload.putShort((short)19939); //yacc
    payload.putShort((short)20043); //zacc
    
    CRC crc = generateCRC(payload.array());
    payload.put((byte)crc.getLSB());
    payload.put((byte)crc.getMSB());
    return payload.array();
}

@Test
public void test(){
    byte[] packet = generateTestPacket();
    for(int i = 0; i < packet.length - 1; i++){
        parser.mavlink_parse_char(packet[i] & 0xFF);
    }
    MAVLinkPacket m = parser.mavlink_parse_char(packet[packet.length - 1] & 0xFF);
    byte[] processedPacket = m.encodePacket();
    assertArrayEquals("msg_hil_state", processedPacket, packet);
}
}
        