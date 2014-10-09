
package com.techjar.jfos2.network.watcher;

import com.techjar.jfos2.player.Player;

/**
 *
 * @author Techjar
 */
public interface Watchable {
    public Watcher getWatcher();
    public Player[] getExcludedPlayers();
    public int getId();
}
