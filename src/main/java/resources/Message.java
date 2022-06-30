package resources;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class Message {

    public enum cTypes {
        GET_PRODUCT_AMOUNT,
        GET_PRODUCT,
        ADD_PRODUCT,
        ADD_PRODUCT_GROUP,
        ADD_PRODUCT_TITLE_TO_GROUP,
        SET_PRODUCT_PRICE,
        EXCEPTION_FROM_SERVER,
        OK
    }

    Integer cType;
    Integer bUserId;
    String message;
    private byte[] encryptedMessageInBytes;
    public static final int BYTES_WITHOUT_MESSAGE = Integer.BYTES + Integer.BYTES;


    public byte[] getEncryptedMessageInBytes() {
        return encryptedMessageInBytes;
    }
    public void setEncryptedMessageInBytes(byte[] encryptedMessageInBytes) {
        this.encryptedMessageInBytes = encryptedMessageInBytes;
    }

    public void setCType(int cType) {
        this.cType = cType;
    }
    public int getCType() {
        return cType;
    }

    public void setBUserId(int bUserId) {
        this.bUserId = bUserId;
    }
    public int getBUserId() {
        return bUserId;
    }

    public String getMessage() {
        return message;
    }


    public Message() { }

    public Message(cTypes cType, Integer bUserId, String message) throws BadPaddingException, IllegalBlockSizeException {
        this.cType = cType.ordinal();
        this.bUserId = bUserId;
        this.message = message;
        encode();
    }


    public byte[] toPacketPart() {
        return ByteBuffer.allocate(fullMessageBytesLength())
                .putInt(cType)
                .putInt(bUserId)
                .put(encryptedMessageInBytes).array();
    }



    public int fullMessageBytesLength() {return encryptedMessageInBytes.length + BYTES_WITHOUT_MESSAGE; }
    public int messageBytesLength() {return encryptedMessageInBytes.length; }


    public void encode() throws BadPaddingException, IllegalBlockSizeException {

        byte[] myMes = message.getBytes(StandardCharsets.UTF_8);
        encryptedMessageInBytes = Cryptor.encryptMessage(myMes);
    }

    public void decode() throws BadPaddingException, IllegalBlockSizeException{

        byte[] decryptedMessage = Cryptor.decryptMessage(encryptedMessageInBytes);
        message = new String(decryptedMessage);
    }

}