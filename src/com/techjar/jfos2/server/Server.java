package com.techjar.jfos2.server;

import com.techjar.jfos2.util.ConfigManager;
import com.techjar.jfos2.util.Constants;
import com.techjar.jfos2.TickCounter;
import com.techjar.jfos2.entity.Entity;
import com.techjar.jfos2.util.ArgumentParser;
import com.techjar.jfos2.util.logging.LogHelper;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

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
    protected ConfigManager config;
    protected List<Entity> entities = new ArrayList<>();
    protected TickCounter tick;
    protected boolean shutdownRequested;

    public Server(boolean singlePlayer) {
        LogHelper.init();
        this.singlePlayer = singlePlayer;
        tick = new TickCounter(Constants.TICK_RATE);
    }

    public static void main(final String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    instance = new Server(false);
                    ArgumentParser.parse(args, new ArgumentParser.Argument(true, "--loglevel") {
                        @Override
                        public void runAction(String paramater) {
                            LogHelper.setLevel(Level.parse(paramater));
                        }
                    });
                    instance.start();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, "Server Thread").start();
    }

    public static Server getInstance() {
        return instance;
    }

    public void run() {
        final long tickTime = 1000000000 / Constants.TICK_RATE;
        final float delta = 1F / Constants.TICK_RATE;
        Iterator it;
        while (!shutdownRequested) {
            long time = System.nanoTime();
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
        if (!singlePlayer) initConfig();
        run();
    }

    private void initConfig() throws UnknownHostException {
        config = new ConfigManager(new File("config.yml"));
        config.defaultProperty("socket.port", Constants.DEFAULT_PORT);
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
