
package com.techjar.jfos2.network;

import com.techjar.jfos2.player.Player;

/**
 * Interface for objects that need data synchronized across the network.
 *
 * @author Techjar
 */
public interface NetworkedObject {
    /**
     * Reads object's data from a {@link PacketBuffer}.
     *
     * @param buffer a {@link PacketBuffer} to read from.
     */
    public void readSyncData(PacketBuffer buffer);

    /**
     * Writes object's data to a {@link PacketBuffer}.
     *
     * @param buffer a {@link PacketBuffer} to write to.
     */
    public void writeSyncData(PacketBuffer buffer);

    /**
     * Should return false if this object's data needs to be resent to clients.
     *
     * @return false if object needs resend.
     */
    public boolean isSynced();

    /**
     * Called when the object's data has been sent to clients.
     */
    public void markSynced();

    /**
     * Should return true if this object should be sent to the passed player.
     *
     * @param player player to be checked against
     * @return true if this object should be sent to the player
     */
    public boolean shouldSendToPlayer(Player player);
}
