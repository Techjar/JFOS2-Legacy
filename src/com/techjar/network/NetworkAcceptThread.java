package com.techjar.network;

import java.net.*;
import java.io.*;
import com.techjar.network.handler.*;

/**
 *
 * @author Techjar
 */
public class NetworkAcceptThread extends Thread {
    private NetworkServer netServer;
    
    
    public NetworkAcceptThread(String name, NetworkServer netServer) {
        super(name);
        this.netServer = netServer;
    }
    
    @Override
    public void run() {
        Socket socket;
        while(netServer.isListening()) {
            try {
                socket = netServer.getServerSocket().accept();
                if (socket == null) continue;
                NetworkServer.pendingConn.add(new NetworkManager(socket, new NetHandlerLogin()));
                System.out.println(socket.getRemoteSocketAddress() + " connected.");
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
