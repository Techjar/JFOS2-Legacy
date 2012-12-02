package com.techjar.jfos2.client.gui;

import static org.lwjgl.opengl.GL11.*;

import com.techjar.jfos2.MathHelper;
import com.techjar.jfos2.Util;
import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;
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

    public GUIComboBox(UnicodeFont font, Color color, GUIBackground guiBg) {
        this.font = font;
        this.color = color;
        this.guiBg = guiBg;
        this.guiBg.setParent(this);
    }
    
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
        if (!Mouse.isButtonDown(0)) {
            if (checkMouseIntersect(getComponentBox())) {
                if (!hovered) Client.client.getSoundManager().playTemporarySound("ui/rollover.wav", false);
                hovered = true;
            }
            else hovered = false;
        }
    }

    @Override
    public void render() {
        guiBg.render();
        RenderHelper.drawSquare(getPosition().getX() + dimension.getWidth() - 20, getPosition().getY(), 20, dimension.getHeight(), guiBg.getBorderColor());
        RenderHelper.setGlColor(hovered || opened ? Util.addColors(guiBg.getBackgroundColor(), new Color(50, 50, 50)) : guiBg.getBackgroundColor());
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_TRIANGLES);
            glVertex2f(getPosition().getX() + dimension.getWidth() - 16, getPosition().getY() + 3);
            glVertex2f(getPosition().getX() + dimension.getWidth() - 3.5f, getPosition().getY() + 3);
            glVertex2f(getPosition().getX() + dimension.getWidth() - 10.25f, getPosition().getY() + dimension.getHeight() - 3);
        glEnd();
        glEnable(GL_TEXTURE_2D);
        if (getSelectedItem() != null) {
            RenderHelper.beginScissor(new Rectangle(getPosition().getX() + guiBg.getBorderSize() + 3, getPosition().getY() + guiBg.getBorderSize() + 3, dimension.getWidth() - 23, dimension.getHeight() - 6));
            font.drawString(getPosition().getX() + guiBg.getBorderSize() + 3, getPosition().getY() + guiBg.getBorderSize() + 3, getSelectedItem().toString(), Util.convertColor(color));
            RenderHelper.endScissor();
        }
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        guiBg.setDimension(dimension);
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
        return getItem(selectedItem);
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
        scrollBox.setHeight(MathHelper.clamp(dimension.getHeight() * items.size(), dimension.getHeight(), dimension.getHeight() * visibleItems));
        return true;
    }

    public boolean addItem(Object item) {
        if (item == null) return false;
        boolean ret = items.add(new GUIComboItem(this, font, color, Util.addColors(guiBg.getBackgroundColor(), new Color(50, 50, 50)), item));
        scrollBox.setHeight(MathHelper.clamp(dimension.getHeight() * items.size(), dimension.getHeight(), dimension.getHeight() * visibleItems));
        return ret;
    }

    public int getItemCount() {
        return items.size();
    }

    public Object removeItem(int index) {
        Object ret = items.remove(index);
        scrollBox.setHeight(MathHelper.clamp(dimension.getHeight() * items.size(), dimension.getHeight(), dimension.getHeight() * visibleItems));
        return ret == null ? null : ((GUIComboItem)ret).getValue();
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
        Object item = items.get(index);
        return item == null ? null : ((GUIComboItem)item).getValue();
    }

    public void clearItems() {
        items.clear();
    }
}
