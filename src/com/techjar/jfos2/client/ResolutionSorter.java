package com.techjar.jfos2.client;

import org.lwjgl.opengl.DisplayMode;
import java.util.Comparator;

/**
 * 
 * @author Techjar
 */
public class ResolutionSorter implements Comparator<DisplayMode> {
    public ResolutionSorter() {
    }

    @Override
    public int compare(DisplayMode o1, DisplayMode o2) {
        if(o1.getWidth() * o1.getHeight() > o2.getWidth() * o2.getHeight()) return 1;
        if(o1.getWidth() * o1.getHeight() < o2.getWidth() * o2.getHeight()) return -1;
        return 0;
    }
}
