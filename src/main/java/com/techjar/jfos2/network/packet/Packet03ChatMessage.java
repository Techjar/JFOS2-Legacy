
package com.techjar.jfos2.network.packet;

import com.techjar.jfos2.network.ConnectionState;
import com.techjar.jfos2.network.Packet;
import com.techjar.jfos2.network.PacketBuffer;
import com.techjar.jfos2.network.handler.NetHandler;

/**
 * @author Techjar
 */
public class Packet03ChatMessage extends Packet {
	public Packet03ChatMessage() {
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
		return ConnectionState.PLAY;
	}
}
