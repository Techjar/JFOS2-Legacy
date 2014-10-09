
package com.techjar.jfos2.network.packet;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.network.ConnectionState;
import com.techjar.jfos2.network.NetworkUtil;
import com.techjar.jfos2.network.Packet;
import com.techjar.jfos2.network.PacketBuffer;
import com.techjar.jfos2.network.handler.NetHandler;
import com.techjar.jfos2.network.watcher.Watcher;
import com.techjar.jfos2.network.watcher.WatcherValue;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public class Packet04WatcherUpdate extends Packet {
    @Getter protected int watcherId;
    @Getter protected byte[] data;

    public Packet04WatcherUpdate() {
    }

    public Packet04WatcherUpdate(int watcherId, byte[] data) {
        this.watcherId = watcherId;
        this.data = data;
    }

    @Override
    @SneakyThrows(IOException.class)
    public void readData(PacketBuffer buffer) {
        watcherId = buffer.readVarInt();
        data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
    }

    @Override
    public void writeData(PacketBuffer buffer) {
        buffer.writeVarInt(watcherId);
        buffer.writeBytes(data);
    }

    @Override
    @SneakyThrows(IOException.class)
    public void processClient(NetHandler handler) {
        Watcher watcher = handler.getNetworkSynchronizer().getWatchable(watcherId).getWatcher();
        @Cleanup("release") PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(data));
        while (buffer.isReadable()) {
            int id = buffer.readUnsignedByte();
            Class type = watcher.getValueType(id);
            Object value = NetworkUtil.unmarshalObject(type, buffer);
            watcher.setValue(new WatcherValue(id, value));
        }
    }

    @Override
    public void processServer(NetHandler handler) {
        // not recieved by server
    }

    @Override
    public ConnectionState getConnectionState() {
        return ConnectionState.PLAY;
    }
}
