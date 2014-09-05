package com.techjar.jfos2.client;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 *
 * @author Techjar
 */
public class InputInfo {
    private Type type;
    private int button;

    public InputInfo(Type type, int button) {
        this.type = type;
        this.button = button;
    }

    public Type getType() {
        return type;
    }

    public int getButton() {
        return button;
    }

    public String getDisplayString() {
        switch (type) {
            case KEYBOARD:
                if (Keyboard.getKeyName(button) == null) return "";
                return Keyboard.getKeyName(button);
            case MOUSE:
                if (Mouse.getButtonName(button) == null) return "";
                return Mouse.getButtonName(button);
            case CONTROLLER:
                Controller con = Client.getInstance().getController(Client.getInstance().getConfigManager().getString("controls.controller"));
                if (con == null || con.getButtonName(button) == null) return "";
                return con.getButtonName(button);
        }
        return "";
    }

    public static InputInfo fromString(String str) {
        if (str == null) return null;
        Type type = Type.fromString(str);
        if (type == null) return null;
        int code = Integer.valueOf(str.substring(1));
        switch (type) {
            case KEYBOARD:
                if (code != Keyboard.KEY_NONE)
                    return new InputInfo(type, code);
            case MOUSE:
                if (code != -1)
                    return new InputInfo(type, code);
            case CONTROLLER:
                Controller con = Client.getInstance().getController(Client.getInstance().getConfigManager().getString("controls.controller"));
                if (code != -1)
                    return new InputInfo(type, code);
                else {
                    
                }
        }
        return null;
    }

    @Override
    public String toString() {
        return new StringBuilder(type.toString()).append(button).toString();
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
        if (this.type != other.type) {
            return false;
        }
        if (this.button != other.button) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 59 * hash + this.button;
        return hash;
    }

    public static enum Type {
        KEYBOARD,
        MOUSE,
        CONTROLLER;

        @Override
        public String toString() {
            switch (this) {
                case KEYBOARD: return "K";
                case MOUSE: return "M";
                case CONTROLLER: return "C";
            }
            return "";
        }

        public static Type fromString(String str) {
            if (str == null || str.length() < 1) return null;
            switch (str.charAt(0)) {
                case 'K': return KEYBOARD;
                case 'M': return MOUSE;
                case 'C': return CONTROLLER;
            }
            return null;
        }
    }
}
