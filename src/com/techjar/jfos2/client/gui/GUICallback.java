package com.techjar.jfos2.client.gui;

/**
 *
 * @author Techjar
 */
public abstract class GUICallback implements Runnable {
    private GUI component;
    private Object[] args;
    
    public GUICallback() {
        this.args = new Object[0];
    }
    
    public GUICallback(Object... args) {
        this.args = args;
    }

    public final GUI getComponent() {
        return component;
    }

    public final void setComponent(GUI component) {
        this.component = component;
    }

    public Object[] getArgs() {
        return args;
    }
}
