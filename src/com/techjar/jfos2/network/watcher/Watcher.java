
package com.techjar.jfos2.network.watcher;

import java.util.List;

/**
 *
 * @author Techjar
 */
public interface Watcher {
    public List<WatcherValue> getAllValues();
    public List<WatcherValue> getChangedValues(boolean markUpdated);
    public Class getValueType(int id);
    public void setValue(WatcherValue value);
    public int getId();
}
