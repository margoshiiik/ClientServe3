package resources;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;


public class Processor extends Thread {

    public static Packet process(Packet inputPacket) throws BadPaddingException, IllegalBlockSizeException {

        Message message = inputPacket.getBMsq();
        Message answerMessage = null;
        String answer;


        if (message.getCType() == Message.cTypes.ADD_PRODUCT.ordinal()) {
            answer = message.getMessage() + " ADD_PRODUCT!";
            answerMessage = new Message(Message.cTypes.ADD_PRODUCT, 0, answer);

        } else if (message.getCType() == Message.cTypes.ADD_PRODUCT_GROUP.ordinal()) {
            answer = message.getMessage() + " ADD_PRODUCT_GROUP!";
            answerMessage = new Message(Message.cTypes.ADD_PRODUCT_GROUP, 0, answer);
        }
        else if (message.getCType() == Message.cTypes.ADD_PRODUCT_TITLE_TO_GROUP.ordinal()) {
            answer = message.getMessage() + " ADD_PRODUCT_TITLE_TO_GROUP!";
            answerMessage = new Message(Message.cTypes.ADD_PRODUCT_TITLE_TO_GROUP, 0, answer);
        }
        else {
            answer = message.getMessage() + " OK!";
            answerMessage = new Message(Message.cTypes.OK, 0, answer);
        }

        Packet answerPacket = new Packet(inputPacket.getbSrc(), inputPacket.getbPktId(), answerMessage);
        return answerPacket;
    }
}
