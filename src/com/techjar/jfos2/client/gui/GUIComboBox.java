package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.MathHelper;
import com.techjar.jfos2.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
    protected GUIScrollBox scrollBox;
    protected List<GUIComboItem> items = new ArrayList<GUIComboItem>();
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
        if (o == null) setSelectedItem(-1);
        else if (o instanceof GUIComboItem) {
            setSelectedItem(items.indexOf(o));
        }
        else {
            GUIComboItem item = null;
            for (GUIComboItem item2 : items) {
                if (o.equals(item2.getValue())) {
                    item = item2;
                    break;
                }
            }
            if (item == null) setSelectedItem(-1);
            else setSelectedItem(items.indexOf(item));
        }
    }

    public Object getSelectedItem() {
        if (selectedItem < 0 || selectedItem >= items.size()) return null;
        return items.get(selectedItem).getValue();
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public boolean addAllItems(int index, Collection<? extends Object> items) {
        boolean modified = false;
        for (Object o : items) {
            if (addItem(index++, o)) modified = true;
        }
        return modified;
    }

    public boolean addAllItems(Collection<? extends Object> items) {
        boolean modified = false;
        for (Object o : items) {
            if (addItem(o)) modified = true;
        }
        return modified;
    }

    public boolean addItem(int index, Object item) {
        if (item == null) return false;
        items.add(index, new GUIComboItem(this, font, color, Util.addColors(guiBg.getBackgroundColor(), new Color(50, 50, 50)), item));
        return true;
    }

    public boolean addItem(Object item) {
        if (item == null) return false;
        return items.add(new GUIComboItem(this, font, color, Util.addColors(guiBg.getBackgroundColor(), new Color(50, 50, 50)), item));
    }

    public int getItemCount() {
        return items.size();
    }

    public Object removeItem(int index) {
        return items.remove(index);
    }

    public boolean removeItem(Object o) {
        if (o == null) return false;
        Iterator it = items.iterator();
        while (it.hasNext()) {
            GUIComboItem item = (GUIComboItem)it.next();
            if (o.equals(item.getValue())) {
                it.remove();
                return true;
            }
        }
        return false;
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
