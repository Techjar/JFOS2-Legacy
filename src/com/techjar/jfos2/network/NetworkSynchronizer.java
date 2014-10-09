
package com.techjar.jfos2.network;

import com.techjar.jfos2.network.packet.Packet04WatcherUpdate;
import com.techjar.jfos2.network.watcher.Watchable;
import com.techjar.jfos2.network.watcher.Watcher;
import com.techjar.jfos2.network.watcher.WatcherValue;
import com.techjar.jfos2.world.World;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Cleanup;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public class NetworkSynchronizer {
    private final List<Watchable> watchables = new ArrayList<>();
    private final Map<Integer, Watchable> watchableMap = new HashMap<>();
    private final NetworkServer network;
    private final World world;

    public NetworkSynchronizer(NetworkServer network, World world) {
        this.network = network;
        this.world = world;
    }

    @SneakyThrows(IOException.class)
    public void tick() {
        for (Watchable watchable : watchables) {
            Watcher watcher = watchable.getWatcher();
            List<WatcherValue> values = watcher.getChangedValues(true);
            if (!values.isEmpty()) {
                @Cleanup("release") PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                for (WatcherValue value : values) {
                    buffer.writeByte(value.getId());
                    NetworkUtil.marshalObject(value.getValue(), buffer);
                }
                Packet04WatcherUpdate packet = new Packet04WatcherUpdate(watchable.getId(), buffer.array());
                network.sendPacketToAllInWorldExcluding(packet, world, watchable.getExcludedPlayers());
            }
        }
    }

    public Watchable getWatchable(int id) {
        return watchableMap.get(id);
    }

    public void addWatchable(Watchable watchable) {
        if (!watchableMap.containsKey(watchable.getId())) {
            watchables.add(watchable);
            watchableMap.put(watchable.getId(), watchable);
        }
    }

    public void removeWatchable(Watchable watchable) {
        watchables.remove(watchable);
        watchableMap.remove(watchable.getId());
    }
}
