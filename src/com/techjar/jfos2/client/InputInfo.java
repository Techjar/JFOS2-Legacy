/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.client;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 *
 * @author Techjar
 */
public class InputInfo {
    private boolean mouse;
    private int button;

    public InputInfo(boolean mouse, int button) {
        this.mouse = mouse;
        this.button = button;
    }

    public boolean isMouse() {
        return mouse;
    }

    public int getButton() {
        return button;
    }

    public static InputInfo fromString(String name) {
        if (Mouse.getButtonIndex(name) != -1) {
            return new InputInfo(true, Mouse.getButtonIndex(name));
        }
        if (Keyboard.getKeyIndex(name) != Keyboard.KEY_NONE) {
            return new InputInfo(false, Keyboard.getKeyIndex(name));
        }
        return null;
    }

    @Override
    public String toString() {
        return mouse ? Mouse.getButtonName(button) : Keyboard.getKeyName(button);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InputInfo other = (InputInfo) obj;
        if (this.mouse != other.mouse) {
            return false;
        }
        if (this.button != other.button) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.mouse ? 1 : 0);
        hash = 97 * hash + this.button;
        return hash;
    }
}
