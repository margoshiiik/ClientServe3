package tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class StoreServerTCP extends Thread {

    public ServerSocket server;

    private int                port;
    private ThreadPoolExecutor connectionPool;
    private ThreadPoolExecutor processPool;
    private int                clientTimeout;

    public StoreServerTCP(int port, int maxConnectionThreads, int maxProcessThreads, int maxClientTimeout)
            throws IOException {
        super("Server");
        if (maxClientTimeout < 0) throw new IllegalArgumentException("timeout can't be negative");
        this.port = port;
        connectionPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxConnectionThreads);
        processPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxProcessThreads);
        this.clientTimeout = maxClientTimeout;
        server = new ServerSocket(port);
    }


    @Override
    public void run() {

        try {
            System.out.println("Server running on port: " + port);

            while (true) {
                connectionPool.execute(new ClientHandler(server.accept(), processPool, clientTimeout));
            }

        } catch (SocketException e) {
            System.out.println("Closing the server");

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            connectionPool.shutdown();
            processPool.shutdown();
            System.out.println("Server has closed");
        }

    }

    public void shutdown() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
