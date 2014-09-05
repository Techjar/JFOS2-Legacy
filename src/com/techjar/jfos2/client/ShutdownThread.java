package com.techjar.jfos2.client;

/**
 *
 * @author Techjar
 */
public class ShutdownThread extends Thread {
    @Override
    public void run() {
        Client.getInstance().shutdown();
    }
}
