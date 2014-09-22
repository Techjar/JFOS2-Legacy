package com.techjar.jfos2;

import com.techjar.jfos2.util.OperatingSystem;
import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.util.ArgumentParser;
import com.techjar.jfos2.util.logging.LogHelper;
import java.io.File;
import java.util.logging.Level;

/**
 *
 * @author Techjar
 */
public class Launcher {
    public static void main(String[] args) {
        LogHelper.init();
        if (OperatingSystem.isUnknown()) {
            LogHelper.severe("Unsupported OS detected, exiting...");
            System.exit(0);
        }
        try {
            File workingDir = new File(System.getProperty("user.dir"));
            File natives = new File(workingDir, "lib/native/" + OperatingSystem.getTypeString());
            System.setProperty("org.lwjgl.librarypath", natives.getPath());
            System.setProperty("net.java.games.input.librarypath", natives.getPath());
            
            Client.main(args);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }
}
