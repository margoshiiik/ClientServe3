package udp;

import resources.Message;
import resources.Packet;
import resources.Processor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class UDPResponder implements Runnable {
    private Packet answerPac = null;
    private DatagramSocket ds;
    private DatagramPacket dp;
    private Object lock = new Object();

    UDPResponder(DatagramPacket dp) {
        this.dp = dp;
    }

    @Override
    public void run() {
        synchronized (lock) {

            Packet pacToBeProcessed = null;
            try {
                pacToBeProcessed = new Packet(dp.getData());
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }

            assert pacToBeProcessed != null;
            if(StoreServerUDP.packetCanBeProcessed(pacToBeProcessed.getBMsq().getBUserId(), pacToBeProcessed.getbPktId())) {
                try {
                    answerPac = Processor.process(pacToBeProcessed);
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }

                try {
                    ds = new DatagramSocket();
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                try {
                    ds.send(new DatagramPacket(answerPac.toPacket(), answerPac.toPacket().length, dp.getAddress(), dp.getPort()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ds.close();


            } else {


                try {
                    answerPac = new Packet(pacToBeProcessed.getbSrc(), pacToBeProcessed.getbPktId(),
                            new Message(Message.cTypes.EXCEPTION_FROM_SERVER, 0, "This packet has been processed yet!"));
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }

                try {
                    ds = new DatagramSocket();
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                try {
                    ds.send(new DatagramPacket(answerPac.toPacket(), answerPac.toPacket().length, dp.getAddress(), dp.getPort()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ds.close();

            }

        }

    }

}
