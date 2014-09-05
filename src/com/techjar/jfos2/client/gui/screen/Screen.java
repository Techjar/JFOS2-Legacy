package com.techjar.jfos2.client.gui.screen;

import com.techjar.jfos2.client.gui.GUI;
import com.techjar.jfos2.client.gui.GUIWindow;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.input.Controller;

/**
 *
 * @author Techjar
 */
public abstract class Screen {
    protected List<GUI> elements;

    public boolean processKeyboardEvent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean processMouseEvent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean processControllerEvent(Controller con) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void update() {
        GUIWindow lastWin = null, lastTopWin = null;
        List<GUI> toAdd = new ArrayList<>();
        Iterator<GUI> it = elements.iterator();
        while (it.hasNext()) {
            GUI gui = it.next();
            if (gui.isRemoveRequested()) it.remove();
            else {
                if (gui.isVisible() && gui.isEnabled()) {
                    gui.update();
                    if (gui.isRemoveRequested()) it.remove();
                    else if (gui instanceof GUIWindow) {
                        GUIWindow win = (GUIWindow)gui;
                        if (lastWin != null && lastWin != lastTopWin) lastWin.setOnTop(false);
                        lastWin = win;
                        win.setOnTop(true);
                        if (win.isToBePutOnTop()) {
                            it.remove();
                            toAdd.add(gui);
                            win.setToBePutOnTop(false);
                            if (lastTopWin != null) lastTopWin.setOnTop(false);
                            lastTopWin = win;
                        }
                    }
                }
            }
        }
        elements.addAll(toAdd);
    }

    public void render() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
