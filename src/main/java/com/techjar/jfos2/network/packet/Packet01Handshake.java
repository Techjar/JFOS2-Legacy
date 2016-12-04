
package com.techjar.jfos2.network.packet;

import com.techjar.jfos2.network.ConnectionState;
import com.techjar.jfos2.network.Packet;
import com.techjar.jfos2.network.PacketBuffer;
import com.techjar.jfos2.network.handler.NetHandler;
import lombok.Getter;

/**
 * @author Techjar
 */
public class Packet01Handshake extends Packet {
	public Packet01Handshake() {
	}

	@Override
	public void readData(PacketBuffer buffer) {
		// TODO
	}

	@Override
	public void writeData(PacketBuffer buffer) {
		// TODO
	}

	@Override
	public void processClient(NetHandler handler) {
		// TODO
	}

	@Override
	public void processServer(NetHandler handler) {
		// TODO
	}

	@Override
	public ConnectionState getConnectionState() {
		return ConnectionState.HANDSHAKE;
	}
}
