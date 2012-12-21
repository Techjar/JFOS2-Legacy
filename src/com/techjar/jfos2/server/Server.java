package com.techjar.jfos2.server;

import com.techjar.jfos2.ConfigManager;
import com.techjar.jfos2.Constants;
import com.techjar.jfos2.Util;
import com.techjar.network.NetworkServer;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Techjar
 */
public class Server {
    public static Server server;
    private int port;
    private InetAddress ip;
    private String name;
    private NetworkServer netServer;
    private ConfigManager config;

    public void Server() {
        
    }

    public static void main(String[] args) {
        try {
            server = new Server();
            server.start();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public void run() {
        
    }

    public void start() throws IOException {
        initConfig();
        netServer = new NetworkServer(null, port);
    }

    private void initConfig() throws UnknownHostException {
        config = new ConfigManager(new File("config.yml"));
        config.defaultProperty("socket.port", Constants.PORT);
        config.defaultProperty("socket.ip", "");

        port = config.getInteger("socket.port");
        ip = InetAddress.getByName(config.getString("socket.ip"));
        config.save();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
