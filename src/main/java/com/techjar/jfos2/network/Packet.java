
package com.techjar.jfos2.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.techjar.jfos2.network.handler.NetHandler;

import java.lang.reflect.Modifier;

import lombok.SneakyThrows;

/**
 * @author Techjar
 */
public abstract class Packet {
	public static final int MAX_SIZE = 2097152;
	private static final BiMap<Integer, Class<? extends Packet>> packetMap = HashBiMap.create();
	private static final BiMap<Integer, Class<? extends Packet>> clientPacketMap = HashBiMap.create();
	private static final BiMap<Integer, Class<? extends Packet>> serverPacketMap = HashBiMap.create();

	@SneakyThrows(Exception.class)
	public static Packet generatePacket(int id, boolean server) {
		Class<? extends Packet> clazz = server ? serverPacketMap.get(id) : clientPacketMap.get(id);
		return clazz == null ? null : clazz.newInstance();
	}

	public static void registerPacket(int id, Class<? extends Packet> clazz, boolean client, boolean server) {
		if (Modifier.isAbstract(clazz.getModifiers()))
			throw new IllegalArgumentException("Cannot register abstract packet class: " + clazz.getName());
		if (packetMap.containsKey(id)) throw new RuntimeException("Packet ID already in use: " + id);
		if (packetMap.containsValue(clazz))
			throw new RuntimeException("Packet class already registered: " + clazz.getName());
		if (client) clientPacketMap.put(id, clazz);
		if (server) serverPacketMap.put(id, clazz);
		packetMap.put(id, clazz);
	}

	public abstract void readData(PacketBuffer buffer);

	public abstract void writeData(PacketBuffer buffer);

	public abstract void processClient(NetHandler handler);

	public abstract void processServer(NetHandler handler);

	public abstract ConnectionState getConnectionState();

	public final void process(NetHandler handler) {
		if (handler.isServer()) {
			this.processServer(handler);
		} else {
			this.processClient(handler);
		}
	}

	public final Integer getId() {
		return packetMap.inverse().get(this.getClass());
	}

	public boolean hasPriority() {
		return false;
	}
}
