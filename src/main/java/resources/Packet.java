package resources;

import com.google.common.primitives.UnsignedLong;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.nio.ByteBuffer;

public class Packet {

    public final static Byte B_MAGIC = 0x13;
    public final static Integer HEADER_LENGTH = 16;
    public final static Integer CRC16_LENGTH = 2;


    UnsignedLong bPktId;
    Byte bSrc;
    Integer wLen;
    Short wCrc16_1;
    Message bMsq;
    Short wCrc16_2;


    public Message getBMsq() {
        return bMsq;
    }

    public Byte getbSrc() { return bSrc; }

    public void setbPktId(UnsignedLong bPktId) {
        this.bPktId = bPktId;
    }

    public UnsignedLong getbPktId() {  return bPktId; }

    public final static Integer packetPartFirstLengthWithoutwLen = B_MAGIC.BYTES + Byte.BYTES + Long.BYTES;
    public final static Integer packetPartFirstLength = packetPartFirstLengthWithoutwLen + Integer.BYTES;
    public final static Integer packetPartFirstLengthWithCRC16 = packetPartFirstLength + Short.BYTES;


    public Packet(Byte bSrc, UnsignedLong bPktId, Message bMsq) {
        this.bSrc = bSrc;
        this.bPktId = bPktId;
        this.bMsq = bMsq;
        wLen = bMsq.fullMessageBytesLength();
    }


    public Packet(byte[] encodedPacket) /*throws Exception */throws BadPaddingException, IllegalBlockSizeException {

        ByteBuffer buffer = ByteBuffer.wrap(encodedPacket);

        Byte expectedBMagic = buffer.get();

//        if (!expectedBMagic.equals(B_MAGIC))
//            throw new Exception("Unexpected bMagic");


        bSrc = buffer.get();
        bPktId = UnsignedLong.fromLongBits(buffer.getLong());
        wLen = buffer.getInt();

        wCrc16_1 = buffer.getShort();

        final short crc1Evaluated = CRC16.evaluateCrc(encodedPacket, 0, 14);
//        if (crc1Evaluated != wCrc16_1) {
//            throw new IllegalArgumentException("CRC1 expected : " + crc1Evaluated + ", but was : " + wCrc16_1);
//        }

        bMsq = new Message();
        bMsq.setCType(buffer.getInt());
        bMsq.setBUserId(buffer.getInt());

        byte[] messageBody = new byte[wLen - 8];
        buffer.get(messageBody);


        wCrc16_2 = buffer.getShort();

        bMsq.setEncryptedMessageInBytes(messageBody);
        bMsq.decode();

        byte[] messageToEvaluate = new byte[wLen];
        System.arraycopy(encodedPacket, 16, messageToEvaluate, 0, wLen);

        final short crc2Evaluated = CRC16.evaluateCrc(messageToEvaluate, 0, wLen);
//        if (crc2Evaluated != wCrc16_2) {
//            throw new IllegalArgumentException("CRC2 expected : " + crc2Evaluated + ", but was : " + wCrc16_2);
//        }
    }


    public byte[] toPacket() {

        Message message = getBMsq();


        byte[] packetPartFirst = ByteBuffer.allocate(packetPartFirstLength)
                .put(B_MAGIC)
                .put(bSrc)
                .putLong(bPktId.longValue())
                .putInt(wLen)
                .array();


        wCrc16_1 = CRC16.evaluateCrc(packetPartFirst, 0, 14);


        Integer packetPartSecondLength = message.fullMessageBytesLength();

        byte[] packetPartSecond = ByteBuffer.allocate(packetPartSecondLength)
                .put(message.toPacketPart())
                .array();


        wCrc16_2 = CRC16.evaluateCrc(packetPartSecond, 0, packetPartSecond.length);


        Integer packetLength = packetPartFirstLength + wCrc16_1.BYTES + packetPartSecondLength + wCrc16_2.BYTES;

        return ByteBuffer.allocate(packetLength).put(packetPartFirst).putShort(wCrc16_1).put(packetPartSecond).putShort(wCrc16_2).array();
    }


//    public static void main(String[] args) throws Exception {
//        Message mes = new Message(1,1,"Hello");
//        Packet pac = new Packet((byte)2, UnsignedLong.ONE, mes);
//
//        Packet newPac = new Packet(pac.toPacket());
//        System.out.println(newPac.getBMsq().getMessage());
//
//    }

}
