package com.techjar.jfos2.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.techjar.jfos2.LongSleeperThread;
import com.techjar.jfos2.util.ConfigManager;
import com.techjar.jfos2.util.Constants;
import com.techjar.jfos2.TickCounter;
import com.techjar.jfos2.entity.Entity;
import com.techjar.jfos2.network.NetworkSynchronizer;
import com.techjar.jfos2.util.ArgumentParser;
import com.techjar.jfos2.util.logging.LogHelper;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public class Server {
    private static Server instance;
    private final boolean local;
    protected int port;
    protected InetAddress ip;
    protected String name;
    protected ConfigManager config;
    protected List<Entity> entities = new ArrayList<>();
    protected TickCounter tick;
    protected static int nextObjectId;
    protected boolean shutdownRequested;

    public Server(boolean local) {
        if (!local) LogHelper.init(new File("logs"));
        this.local = local;
        tick = new TickCounter(Constants.TICK_RATE);
    }

    public static void main(final String[] args) {
        ArgumentParser.parse(args, new ArgumentParser.Argument(true, "--loglevel") {
            @Override
            public void runAction(String paramater) {
                LogHelper.setLevel(Level.parse(paramater));
            }
        });
        startThread(false);
    }

    public static Server getInstance() {
        return instance;
    }

    protected final void setInstance(Server server) {
        instance = server;
    }

    public void runTick() {
        try {
            float delta = 1F / Constants.TICK_RATE;
            if (!shutdownRequested) {
                // Begin Game Logic

                // End Game Logic
                tick.incTicks();
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // TODO: Better error handling.
            shutdownRequested = true;
        }
    }

    @SneakyThrows(IOException.class)
    public static void startThread(final boolean local) {
        if (instance != null) throw new IllegalStateException("Server already running!");
        LongSleeperThread.startSleeper();
        nextObjectId = 0;
        instance = new Server(local);
        instance.start();
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("Server Thread").build());
        final long tickTime = 1000000000 / Constants.TICK_RATE;
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                instance.runTick();
            }
        }, 0, tickTime, TimeUnit.NANOSECONDS);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!instance.shutdownRequested) {
                        Thread.sleep(100);
                    }
                    executor.shutdown();
                    executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                    instance = null;
                } catch (InterruptedException ex) {}
            }
        }, "Server Wait Thread").start();
    }

    public void start() throws IOException {
        if (!local) initConfig();
    }

    public void shutdown() {
        shutdownRequested = true;
    }

    protected void initConfig() throws UnknownHostException {
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

    public boolean isLocal() {
        return local;
    }

    public static int getNextObjectId() {
        return nextObjectId++;
    }
}
