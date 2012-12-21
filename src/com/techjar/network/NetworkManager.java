package com.techjar.network;

import java.util.*;
import java.net.*;
import java.io.*;
import com.techjar.network.handler.*;
import com.techjar.network.packet.*;

/**
 *
 * @author Techjar
 */
public class NetworkManager {
    public static final Object threadSync = new Object();
    public static volatile int numReadThreads = 0;
    public static volatile int numWriteThreads = 0;
    public static volatile long connNumber = 0;
    
    private Socket socket;
    private SocketAddress socketAddress;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Thread readThread;
    private Thread writeThread;
    private Thread keepAliveThread;
    private Thread processThread;
    private NetHandler netHandler;
    private final List<Packet> sendQueue;
    private final List<Packet> recieveQueue;
    private volatile boolean isTerminating;
    private volatile boolean isTerminated;
    private volatile long timeLastRead = System.currentTimeMillis();
    private volatile int sendQueueLength;
    private final int sendQueueMax;
    public volatile long lastKeepAlive;
    public volatile int ping;
    public final long connectionNumber;
    public NetworkUser user;
    public String terminationReason;
    public Object[] terminationInfo;
    
    
    public NetworkManager(Socket socket, NetHandler handler) throws IOException {
        isTerminating = false;
        sendQueueMax = 0x100000;
        sendQueue = Collections.synchronizedList(new LinkedList<Packet>());
        recieveQueue = Collections.synchronizedList(new LinkedList<Packet>());
        this.socket = socket;
        try {
            socket.setSoTimeout(30000);
            socket.setTrafficClass(0x08 | 0x10);
        }
        catch(SocketException ex) {
            ex.printStackTrace();
        }
        socketAddress = socket.getRemoteSocketAddress();
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 8192));
        netHandler = handler;
        netHandler.netManager = this;
        connectionNumber = connNumber;
        readThread = new NetworkReaderThread("Network Reader Thread #" + connNumber, this);
        writeThread = new NetworkWriterThread("Network Writer Thread #" + connNumber, this);
        readThread.start();
        writeThread.start();
        connNumber++;
        
        keepAliveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!isTerminating) {
                    try {
                        Thread.sleep(5000);
                        queuePacket(new Packet0KeepAlive());
                        lastKeepAlive = System.currentTimeMillis();
                    }
                    catch (Exception e) { }
                }
            }
        });
        keepAliveThread.start();
        
        /*processThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long start;
                while(!isTerminating || recieveQueue.size() > 0) {
                    try {
                        start = System.currentTimeMillis();
                        processPackets();
                        Thread.sleep(50 - (System.currentTimeMillis() - start));
                    }
                    catch (Exception e) { }
                }
            }
        });
        processThread.start();*/
    }
    
    public void queuePacket(Packet packet) {
        if (isTerminating) return;
        queuePacketInternal(packet);
    }
    
    private void queuePacketInternal(Packet packet) {
        sendQueueLength += 1 + packet.getSize();
        sendQueue.add(packet);
    }
    
    public boolean readPacket() {
        try {
            Packet packet = Packet.readPacket(inputStream, netHandler.isServer());
            if (packet != null) {
                recieveQueue.add(packet);
                return true;
            }
            else shutdown("Reached end of stream!");
        }
        catch (Exception ex) {
            networkError(ex);
        }
        return false;
    }
    
    public boolean sendPacket() {
        try {
            if (!sendQueue.isEmpty()) {
                Packet packet = sendQueue.remove(0);
                sendQueueLength -= 1 + packet.getSize();
                Packet.writePacket(outputStream, packet);
                return true;
            }
        }
        catch (Exception ex) {
            networkError(ex);
        }
        return false;
    }
    
    public void processPackets() {
        if (sendQueueLength > sendQueueMax) {
            shutdown("Send buffer overflow!");
            return;
        }
        if (recieveQueue.isEmpty()) {
            if (System.currentTimeMillis() - timeLastRead > 60000) {
                shutdown("Connection timed out!");
                return;
            }
        }
        else timeLastRead = System.currentTimeMillis();
        
        synchronized (recieveQueue) {
            Packet packet;
            for(int i = 0; !recieveQueue.isEmpty() && i < 1000; i++)
            {
                packet = recieveQueue.remove(0);
                packet.process(netHandler);
            }
        }
    }
    
    public void shutdown(String reason, Object... info) {
        if (isTerminating) return;
        isTerminating = true;
        terminationReason = reason;
        terminationInfo = info;
        
        try {
            processThread.interrupt();
            processThread = null;
        }
        catch (Exception ex) { }
        try {
            keepAliveThread.interrupt();
            keepAliveThread = null;
        }
        catch (Exception ex) { }
        try {
            readThread.interrupt();
            readThread = null;
        }
        catch (Exception ex) { }
        try {
            writeThread.interrupt();
            writeThread = null;
        }
        catch (Exception ex) { }
        try {
            inputStream.close();
            inputStream = null;
        }
        catch (Exception ex) { }
        try {
            outputStream.close();
            outputStream = null;
        }
        catch (Exception ex) { }
        try {
            socket.close();
            socket = null;
        }
        catch (Exception ex) { }
        isTerminated = true;
        netHandler.handleNetworkShutdown(terminationReason, terminationInfo);
        if (user != null) {
            NetworkUser.users.remove(user);
            NetworkUser.globalPacket(new Packet4UserList(user.username, false));
            String leaveMsg = new StringBuilder(user.username).append(" has left the chat.").toString();
            System.out.println(leaveMsg);
            NetworkUser.globalMessage(leaveMsg);
        }
        else System.out.println(getRemoteAddress() + " disconnected.");
    }
    
    public void networkError(Exception ex) {
        if (!isTerminating) {
            shutdown("Internal exception!", new StringBuilder("Exception information: ").append(ex.toString()).toString());
            ex.printStackTrace();
        }
    }
    
    public NetHandler getNetHandler() {
        return netHandler;
    }
    
    public void setNetHandler(NetHandler handler) {
        netHandler = handler;
        netHandler.netManager = this;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public SocketAddress getRemoteAddress() {
        return socketAddress;
    }
    
    public DataInputStream getInputStream() {
        return inputStream;
    }
    
    public DataOutputStream getOutputStream() {
        return outputStream;
    }
    
    public Thread getReadThread() {
        return readThread;
    }
    
    public Thread getWriteThread() {
        return writeThread;
    }
    
    public boolean isRunning() {
        return !isTerminating;
    }
    
    public boolean isTerminated() {
        return isTerminated;
    }
}
