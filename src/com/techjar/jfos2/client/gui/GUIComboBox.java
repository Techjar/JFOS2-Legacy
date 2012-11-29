package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.MathHelper;
import com.techjar.jfos2.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public class GUIComboBox extends GUI {
    protected UnicodeFont font;
    protected Color color;
    protected GUIBackground guiBg;
    protected List<Object> items = new ArrayList<Object>();
    protected int visibleItems = 5;
    
    protected int selectedItem = -1;
    protected boolean opened;
    
    @Override
    public boolean processKeyboardEvent() {
        if (Mouse.getEventButton() == 0) {
            
        }
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        return true;
    }

    @Override
    public void update() {
        
    }

    @Override
    public void render() {
        if (getSelectedItem() != null) font.drawString(getPosition().getX() + 5, getPosition().getY() + 5, getSelectedItem().toString(), Util.convertColor(color));
    }

    @Override
    public Shape getComponentBox() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getVisibleItems() {
        return visibleItems;
    }

    public void setVisibleItems(int visibleItems) {
        this.visibleItems = visibleItems;
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
