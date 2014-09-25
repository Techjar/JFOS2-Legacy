
package com.techjar.jfos2.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.techjar.jfos2.network.handler.NetHandler;
import java.lang.reflect.Modifier;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public abstract class Packet {
    private static final BiMap<Integer, Class<? extends Packet>> packetMap = HashBiMap.create();

    @SneakyThrows(Exception.class)
    public static Packet generatePacket(int id) {
        Class<? extends Packet> clazz = packetMap.get(id);
        return clazz == null ? null : clazz.newInstance();
    }

    public static void registerPacket(int id, Class<? extends Packet> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) throw new IllegalArgumentException("Cannot register abstract packet class: " + clazz.getName());
        if (packetMap.containsKey(id)) throw new RuntimeException("Packet ID already in use: " + id);
        if (packetMap.inverse().containsKey(clazz)) throw new RuntimeException("Packet class already registered: " + clazz.getName());
        packetMap.put(id, clazz);
    }

    public abstract void readData(PacketBuffer buffer);
    public abstract void writeData(PacketBuffer buffer);
    public abstract void processClient(NetHandler handler);
    public abstract void processServer(NetHandler handler);

    public int getId() {
        return packetMap.inverse().get(this.getClass());
    }

    public boolean hasPriority() {
        return false;
    }
}
