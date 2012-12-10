package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.Util;
import static org.lwjgl.opengl.GL11.*;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public abstract class GUIContainer extends GUI {
    protected List<GUI> components;
    
    public GUIContainer() {
        components = new ArrayList<GUI>();
    }
    
    @Override
    public boolean processKeyboardEvent() {
        for (GUI gui : components)
            if (gui.isVisible() && !gui.processKeyboardEvent()) return false;
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        for (GUI gui : components)
            if (gui.isVisible() && !gui.processMouseEvent()) return false;
        return true;
    }
    
    @Override
    public void update() {
        Iterator it = components.iterator();
        while (it.hasNext()) {
            GUI gui = (GUI)it.next();
            if (gui.isRemoveRequested()) it.remove();
            else {
                if (gui.isVisible()) {
                    gui.update();
                    if (gui.isRemoveRequested()) it.remove();
                }
            }
        }
    }

    @Override
    public void render() {
        RenderHelper.beginScissor(getScissorBox());
        for (GUI gui : components) {
            if (gui.isVisible()) gui.render();
        }
        RenderHelper.endScissor();
    }
    
    @Override
    public void remove() {
        this.removeAllComponents();
        super.remove();
    }

    @Override
    public Rectangle getContainerBox() {
        return new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
    }
    
    protected Rectangle getScissorBox() {
        return getContainerBox();
    }
    
    public boolean containsComponent(GUI component) {
        return components.contains(component);
    }
    
    public GUI getComponent(int index) {
        return components.get(index);
    }
    
    public List<GUI> getAllComponents() {
        return Collections.unmodifiableList(components);
    }

    public void addComponent(GUI component) {
        components.add(component);
        component.setParent(this);
    }
    
    public void removeComponent(GUI component) {
        components.remove(component);
        component.setParent(null);
    }
    
    public void removeComponent(int index) {
        removeComponent(components.get(index));
    }
    
    public void removeAllComponents() {
        Iterator it = components.iterator();
        while (it.hasNext()) {
            GUI gui = (GUI)it.next();
            gui.setParent(null);
            it.remove();
        }
    }
}
