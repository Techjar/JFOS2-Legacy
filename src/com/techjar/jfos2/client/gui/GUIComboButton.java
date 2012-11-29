package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.MathHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.lwjgl.util.Color;
import org.newdawn.slick.UnicodeFont;

/**
 *
 * @author Techjar
 */
public class GUIComboButton extends GUI {
    protected List<Object> items = new ArrayList<Object>();

    protected int selectedItem = -1;

    public GUIComboButton(UnicodeFont font, Color color, String text) {
        //super(font, color, text);
    }

    @Override
    public boolean processKeyboardEvent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean processMouseEvent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void render() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = MathHelper.clamp(selectedItem, -1, items.size() - 1);
    }

    public void setSelectedItem(Object o) {
        setSelectedItem(items.indexOf(o));
    }

    public Object getSelectedItem() {
        if (selectedItem < 0 || selectedItem >= items.size()) return null;
        return items.get(selectedItem);
    }

    public boolean addAllItems(int index, Collection<? extends Object> c) {
        return items.addAll(index, c);
    }

    public boolean addAllItems(Collection<? extends Object> c) {
        return items.addAll(c);
    }

    public void addItem(int index, Object element) {
        items.add(index, element);
    }

    public boolean addItem(String e) {
        return items.add(e);
    }

    public int getItemCount() {
        return items.size();
    }

    public Object removeItem(int index) {
        return items.remove(index);
    }

    public boolean removeItem(Object o) {
        return items.remove(o);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Object getItem(int index) {
        return items.get(index);
    }

    public void clearItems() {
        items.clear();
    }
}
