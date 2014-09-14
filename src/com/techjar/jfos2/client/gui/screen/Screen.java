package com.techjar.jfos2.client.gui.screen;

import com.techjar.jfos2.client.gui.GUI;
import com.techjar.jfos2.client.gui.GUIWindow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.input.Controller;

/**
 *
 * @author Techjar
 */
public abstract class Screen {
    protected List<GUI> components;
    private boolean removeRequested;

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
        Iterator<GUI> it = components.iterator();
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
        components.addAll(toAdd);
    }

    public void render() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<GUI> getComponentList() {
        return Collections.unmodifiableList(components);
    }

    public void addComponent(GUI component) {
        components.add(component);
        component.setScreen(this);
    }

    public void removeComponent(GUI component) {
        components.remove(component);
        component.setScreen(null);
    }

    public GUI removeComponent(int index) {
        GUI component = components.get(index);
        if (component != null) removeComponent(component);
        return component;
    }

    public void removeAllComponents() {
        Iterator it = components.iterator();
        while (it.hasNext()) {
            GUI gui = (GUI)it.next();
            gui.setScreen(null);
            it.remove();
        }
    }
}
