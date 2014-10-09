
package com.techjar.jfos2.network.packet;

import com.techjar.jfos2.network.ConnectionState;
import com.techjar.jfos2.network.Packet;
import com.techjar.jfos2.network.PacketBuffer;
import com.techjar.jfos2.network.handler.NetHandler;
import lombok.Getter;

/**
 *
 * @author Techjar
 */
public class Packet00KeepAlive extends Packet {
    @Getter protected int randomId;

    public Packet00KeepAlive() {
    }

    public Packet00KeepAlive(int randomId) {
        this.randomId = randomId;
    }

    @Override
    public void readData(PacketBuffer buffer) {
        randomId = buffer.readInt();
    }

    @Override
    public void writeData(PacketBuffer buffer) {
        buffer.writeInt(randomId);
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
