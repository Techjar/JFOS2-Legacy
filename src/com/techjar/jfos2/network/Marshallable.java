
package com.techjar.jfos2.network;

/**
 *
 * @author Techjar
 */
public interface Marshallable {
    public void readData(PacketBuffer buffer);
    public void writeData(PacketBuffer buffer);
}
