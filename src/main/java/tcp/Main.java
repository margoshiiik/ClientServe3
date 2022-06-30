package tcp;

import com.google.common.primitives.UnsignedLong;
import resources.Message;
import resources.Packet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Starting\n");

        int port = 12345;

        StoreServerTCP server = null;
        try {
            server = new StoreServerTCP(port, 40, 10, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Packet packet1 = null;

        Packet packet2 = null;

        Packet packet3 = null;

        Packet packet4 = null;
        try {
            packet1 =
                    new Packet((byte) 1, UnsignedLong.ONE, new Message(Message.cTypes.ADD_PRODUCT_GROUP, 1, "client1"));
            packet2 = new Packet((byte) 1, UnsignedLong.ONE, new Message(Message.cTypes.ADD_PRODUCT, 1, "client2"));
            packet3 = new Packet((byte) 1, UnsignedLong.ONE, new Message(Message.cTypes.GET_PRODUCT_AMOUNT, 1, "client3"));
            packet4 = new Packet((byte) 1, UnsignedLong.ONE, new Message(Message.cTypes.ADD_PRODUCT_TITLE_TO_GROUP, 1, "client4"));
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        StoreClientTCP client1 = new StoreClientTCP(port, packet1);

        StoreClientTCP client2 = new StoreClientTCP(port, packet2);

        StoreClientTCP client3 = new StoreClientTCP(port, packet3);

        StoreClientTCP client4 = new StoreClientTCP(port, packet4);

        client1.start();
        client2.start();
        client3.start();
        client4.start();


        try {
            Thread.sleep(3_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.shutdown();


        System.out.println("\nEnd");
    }

}
