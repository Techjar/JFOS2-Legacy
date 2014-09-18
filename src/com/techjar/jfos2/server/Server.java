package com.techjar.jfos2.server;

import com.techjar.jfos2.util.ConfigManager;
import com.techjar.jfos2.util.Constants;
import com.techjar.jfos2.TickCounter;
import com.techjar.jfos2.entity.Entity;
import com.techjar.jfos2.util.logging.LogHelper;
import com.techjar.network.NetworkManager;
import com.techjar.network.NetworkServer;
import com.techjar.network.NetworkUser;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Techjar
 */
public class Server {
    private static Server instance;
    private final boolean singlePlayer;
    protected int port;
    protected InetAddress ip;
    protected String name;
    protected NetworkServer netServer;
    protected ConfigManager config;
    protected List<Entity> entities = new ArrayList<>();
    protected TickCounter tick;
    protected boolean shutdownRequested;

    public Server(boolean singlePlayer) {
        LogHelper.init();
        this.singlePlayer = singlePlayer;
        tick = new TickCounter(Constants.TICK_RATE);
    }

    public static void main(String[] args) {
        try {
            instance = new Server(false);
            instance.start();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Server getInstance() {
        return instance;
    }

    public void run() {
        Iterator it; final long tickTime = 1000000000 / Constants.TICK_RATE;
        while (!shutdownRequested) {
            long time = System.nanoTime();
            it = NetworkServer.pendingConn.iterator(); while(it.hasNext()) ((NetworkManager)it.next()).processPackets();
            it = NetworkUser.users.iterator(); while(it.hasNext()) ((NetworkUser)it.next()).netManager.processPackets();
            // Begin Game Logic

            // End Game Logic
            tick.incTicks();
            long sleepTime = tickTime - (System.nanoTime() - time);
            long millis = sleepTime / 1000000;
            try { Thread.sleep(millis, (int)(sleepTime - (millis * 1000000))); }
            catch (InterruptedException ex) { }
        }
    }

    public void start() throws IOException {
        initConfig();
        netServer = new NetworkServer(ip, port);
        run();
        netServer.shutdown();
    }

    private void initConfig() throws UnknownHostException {
        config = new ConfigManager(new File("config.yml"));
        config.defaultProperty("socket.port", Constants.PORT);
        config.defaultProperty("socket.ip", "");

        port = config.getInteger("socket.port");
        ip = config.getString("socket.ip").trim().isEmpty() ? null : InetAddress.getByName(config.getString("socket.ip"));
        if (config.hasChanged()) config.save();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSinglePlayer() {
        return singlePlayer;
    }
}
