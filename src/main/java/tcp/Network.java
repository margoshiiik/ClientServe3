package tcp;

import resources.CRC16;
import resources.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;


public class Network {

    private Socket       socket;
    private InputStream  inputStream;
    private OutputStream outputStream;

    private int maxTimeout;

    private Semaphore outputStreamLock = new Semaphore(1);
    private Semaphore inputStreamLock  = new Semaphore(1);


    public Network(Socket socket, int maxTimeout) throws IOException {
        if(maxTimeout < 100){
            throw new IllegalArgumentException("timeout can't be < 100");
        }
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        this.maxTimeout = maxTimeout;
    }

    public byte[] receive() throws IOException {
        try {
            inputStreamLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            ArrayList<Byte>     receivedBytes = new ArrayList<>(Packet.HEADER_LENGTH * 3);
            LinkedList<Integer> bMagicIndexes = new LinkedList<>();

            int    wLen    = 0;
            byte[] oneByte = new byte[1];

            byte[] packetBytes;

            int noNewData = 0;

            while (true) {
                if (inputStream.available() == 0) {
                    if (noNewData == maxTimeout/100) {
                        return null;
                    }
                    ++noNewData;

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                inputStream.read(oneByte);
                noNewData = 0;

                if (Packet.B_MAGIC.equals(oneByte[0]) && receivedBytes.size() > 0) {
                    bMagicIndexes.add(receivedBytes.size());
                }
                receivedBytes.add(oneByte[0]);

                if (receivedBytes.size() == Packet.HEADER_LENGTH + wLen + Packet.CRC16_LENGTH) {
                    final short wCrc16_2 = ByteBuffer.allocate(2).put(receivedBytes.get(receivedBytes.size() - 2))
                            .put(receivedBytes.get(receivedBytes.size() - 1)).rewind().getShort();

                    packetBytes = toPrimitiveByteArr(receivedBytes.toArray(new Byte[0]));
                    final short crc2Evaluated =
                            CRC16.evaluateCrc(packetBytes, Packet.HEADER_LENGTH, receivedBytes.size() - 2);

                    if (wCrc16_2 == crc2Evaluated) {
                        receivedBytes.clear();
                        bMagicIndexes.clear();
                        return packetBytes;

                    } else {
                        wLen = 0;
                        receivedBytes = resetToFirstBMagic(receivedBytes, bMagicIndexes);
                    }

                    //check header
                } else if (receivedBytes.size() >= Packet.HEADER_LENGTH) {

                    final short wCrc16_1 = ByteBuffer.allocate(2).put(receivedBytes.get(Packet.HEADER_LENGTH - 2))
                            .put(receivedBytes.get(Packet.HEADER_LENGTH - 1)).rewind().getShort();

                    final short crc1Evaluated =
                            CRC16.evaluateCrc(toPrimitiveByteArr(receivedBytes.toArray(new Byte[0])), 0, 14);

                    if (wCrc16_1 == crc1Evaluated) {
                        wLen = ByteBuffer.allocate(4).put(receivedBytes.get(10)).put(receivedBytes.get(11))
                                .put(receivedBytes.get(12)).put(receivedBytes.get(13)).rewind().getInt();

                    } else {
                        receivedBytes = resetToFirstBMagic(receivedBytes, bMagicIndexes);
                    }
                }
            }
        } finally {
            inputStreamLock.release();
        }
    }


    public void send(byte[] msg) throws IOException {
        try {
            outputStreamLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        outputStream.write(msg);

        outputStreamLock.release();
    }

    public void shutdown() {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private ArrayList<Byte> resetToFirstBMagic(ArrayList<Byte> receivedBytes, LinkedList<Integer> bMagicIndexes) {

        if (!bMagicIndexes.isEmpty()) {
            int firstMagicByteIndex = bMagicIndexes.poll();

            ArrayList<Byte> res = new ArrayList<>(receivedBytes.size());

            for (int i = firstMagicByteIndex; i < receivedBytes.size(); ++i) {
                res.add(receivedBytes.get(i));
            }
            return res;

        } else {
            receivedBytes.clear();
            return receivedBytes;
        }
    }

    private byte[] toPrimitiveByteArr(Byte[] objArr) {
        byte[] primitiveArr = new byte[objArr.length];

        for (int i = 0; i < objArr.length; ++i) {
            primitiveArr[i] = objArr[i];
        }

        return primitiveArr;
    }

}
