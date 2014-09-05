package com.techjar.jfos2;

import com.techjar.jfos2.client.Client;
import java.io.File;

/**
 *
 * @author Techjar
 */
public class Launcher {
    public static void main(String[] args) {
        if (OperatingSystem.isUnknown()) {
            System.out.println("Unsupported OS detected, exiting...");
            System.exit(0);
        }
        try {
            File workingDir = new File(System.getProperty("user.dir"));
            File natives = new File(workingDir, "lib/native/" + OperatingSystem.getTypeString());
            System.setProperty("org.lwjgl.librarypath", natives.getPath());
            System.setProperty("net.java.games.input.librarypath", natives.getPath());
            
            Client.main(args);
            //FBOTest.main(args);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }
}
