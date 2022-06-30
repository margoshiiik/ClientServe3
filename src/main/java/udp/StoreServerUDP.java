package udp;

import com.google.common.primitives.UnsignedLong;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class StoreServerUDP extends Thread {
    private DatagramSocket datagramSocket = null;
    private int listenPort;

    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

    static ConcurrentHashMap<Integer, UnsignedLong> prev = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Integer, UnsignedLong> now = new ConcurrentHashMap<>();


    public StoreServerUDP(int listenPort) {
        this.listenPort = listenPort;
        try {
            datagramSocket = new DatagramSocket(listenPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            datagramSocket.setSoTimeout(7000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.start();
    }

    @Override
    public void run() {
        System.out.println("Server running on port: " + listenPort);

        ClientMapCleaner cmap = new ClientMapCleaner();

        while (true) {

            byte[] buffer = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                System.out.println("Server Socket timed out!");
                executor.shutdown();
                datagramSocket.close();
                cmap.isActive = false;
                System.out.println("Waiting for ClientMapCleaner to shutdown..");
                break;
            }

            executor.execute(new UDPResponder(datagramPacket));
        }

    }

    public synchronized static boolean packetCanBeProcessed(Integer userId, UnsignedLong packetId) {
        if (now.containsKey(userId)) {
            if (now.get(userId).compareTo(packetId) >= 0) return false;
            now.put(userId, packetId);
            return true;
        } else if (!now.containsKey(userId)) {
            prev.put(userId, packetId);
            now.put(userId, packetId);
            return true;
        }
        return false;
    }

    public synchronized static void clearMaps() {
        for (Integer userId : now.keySet()) {
            if (prev.containsKey(userId) && prev.get(userId).compareTo(now.get(userId)) == 0) {
                prev.remove(userId);
                now.remove(userId);
            }
        }
        prev.putAll(now);
    }


}
