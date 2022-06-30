package udp;

import com.google.common.primitives.UnsignedLong;
import resources.Message;
import resources.Packet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;


public class MainUDPTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Staring\n");

        int port = 12345;

        Packet pac0 = null;
        Packet pac1 = null;
        Packet pac2 = null;
        Packet pac3 = null;
        Packet pac4 = null;

        try {
            pac0 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT_GROUP, 0, "client 0"));
            pac1 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT, 1, "client 1"));
            pac2 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.GET_PRODUCT_AMOUNT, 2, "client 2"));
            pac3 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.ADD_PRODUCT_TITLE_TO_GROUP, 3, "client 3"));
            pac4 = new Packet((byte) 1, UnsignedLong.ONE,
                    new Message(Message.cTypes.SET_PRODUCT_PRICE, 4, "client 4"));
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        StoreServerUDP ss = new StoreServerUDP(port);

        StoreClientUDP sc0 = new StoreClientUDP(port, pac0);
        StoreClientUDP sc1 = new StoreClientUDP(port, pac1);
        StoreClientUDP sc2 = new StoreClientUDP(port, pac2);
        StoreClientUDP sc3 = new StoreClientUDP(port, pac3);
        StoreClientUDP sc4 = new StoreClientUDP(port, pac4);



        ss.join();


        System.out.println("\nEnd");
    }

}
