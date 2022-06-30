package tcp;

import com.google.common.primitives.UnsignedLong;
import resources.Message;
import resources.Packet;
import resources.Processor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable {

    private Network network;

    private ThreadPoolExecutor executor;

    private AtomicInteger processingAmount = new AtomicInteger(0);

    public ClientHandler(Socket clientSocket, ThreadPoolExecutor executor, int maxTimeout) throws IOException {
        network = new Network(clientSocket, maxTimeout);
        this.executor = executor;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(Thread.currentThread().getName() + " - ClientHandler");
        try {
            Packet helloPacket = null;
            try {
                helloPacket = new Packet((byte) 0, UnsignedLong.fromLongBits(0),
                        new Message(Message.cTypes.OK, 0, "connection established"));
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
            network.send(helloPacket.toPacket());

            while (true) {
                byte[] packetBytes = network.receive();
                if (packetBytes == null) {
                    System.out.println("client timeout");
                    break;
                }
                handlePacketBytes(Arrays.copyOf(packetBytes, packetBytes.length));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private void handlePacketBytes(byte[] packetBytes) {
        processingAmount.incrementAndGet();

        CompletableFuture.supplyAsync(() -> {
            Packet packet = null;
            try {
                packet = new Packet(packetBytes);
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return packet;
        }, executor)

                .thenAcceptAsync((inputPacket -> {
                    Packet answerPacket = null;
                    try {
                        answerPacket = Processor.process(inputPacket);
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                        System.err.println("BadPaddingException");
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                        System.err.println("IllegalBlockSizeException");
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        System.err.println("NullPointerException");
                    }

                    try {
                        network.send(answerPacket.toPacket());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    processingAmount.decrementAndGet();

                }), executor)

                .exceptionally(ex -> {
                    ex.printStackTrace();
                    processingAmount.decrementAndGet();
                    return null;
                });
    }


    public void shutdown() {
        while (processingAmount.get() > 0) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        network.shutdown();
    }


}
