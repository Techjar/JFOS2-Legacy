package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.client.Client;
import java.util.List;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public abstract class GUI {
    protected Vector2f position;
    protected Dimension dimension;
    protected GUICallback dimensionChangeHandler;
    protected GUICallback positionChangeHandler;
    protected GUIAlignment parentAlign = GUIAlignment.TOP_LEFT;
    protected GUI parent;
    protected boolean removeRequested;
    protected boolean hovered;
    
    public GUI() {
        position = new Vector2f();
        dimension = new Dimension();
    }
    
    public abstract boolean processKeyboardEvent();
    public abstract boolean processMouseEvent();
    public abstract void update();
    public abstract void render();
    
    /**
     * Returns the position of this component relative to it's parent.
     * For the non-relative position, use {@link #getRawPosition}.
     * 
     * @return The position of this component as a {@link Vector2f}
     */
    public Vector2f getPosition() {
        if (parent != null) {
            Vector2f parentPos = parent.getContainerPosition();
            Dimension parentDim = parent.getDimension();
            switch (parentAlign) {
                case TOP_LEFT:
                    return Vector2f.add(position, parentPos, null);
                case TOP_RIGHT:
                    return new Vector2f(position.x + parentPos.x + parentDim.getWidth() - dimension.getWidth(), position.y + parentPos.y);
                case BOTTOM_LEFT:
                    return new Vector2f(position.x + parentPos.x, -position.y + parentPos.y + parentDim.getHeight() - dimension.getHeight());
                case BOTTOM_RIGHT:
                    return new Vector2f(position.x + parentPos.x + parentDim.getWidth() - dimension.getWidth(), position.y + parentPos.y + parentDim.getHeight() - dimension.getHeight());
                case TOP_CENTER:
                    return new Vector2f(position.x + parentPos.x + (parentDim.getWidth() / 2) - (dimension.getWidth() / 2), position.y + parentPos.y);
                case BOTTOM_CENTER:
                    return new Vector2f(position.x + parentPos.x + (parentDim.getWidth() / 2) - (dimension.getWidth() / 2), -position.y + parentPos.y + parentDim.getHeight() - dimension.getHeight());
                case LEFT_CENTER:
                    return new Vector2f(position.x + parentPos.x, position.y + parentPos.y + (parentDim.getHeight() / 2) - (dimension.getHeight() / 2));
                case RIGHT_CENTER:
                    return new Vector2f(position.x + parentPos.x + parentDim.getWidth() - dimension.getWidth(), position.y + parentPos.y + (parentDim.getHeight() / 2) - (dimension.getHeight() / 2));
                case CENTER:
                    return new Vector2f(position.x + parentPos.x + (parentDim.getWidth() / 2) - (dimension.getWidth() / 2), position.y + parentPos.y + (parentDim.getHeight() / 2) - (dimension.getHeight() / 2));
                default:
                    throw new RuntimeException("Illegal value for parentAlign");
            }
        }
        return new Vector2f(position);
    }
    
    public Vector2f getContainerPosition() {
        return getPosition();
    }
    
    /**
     * Returns the position of this component as set by {@link #setPosition}.
     * For the non-relative position, use {@link #getPosition}.
     * 
     * @return The position of this component as a {@link Vector2f}
     */
    public Vector2f getRawPosition() {
        return new Vector2f(position);
    }
    
    public void setPosition(Vector2f position) {
        this.position.set(position);
        if (positionChangeHandler != null) {
            positionChangeHandler.setComponent(this);
            positionChangeHandler.run();
        }
    }
    
    public void setPosition(float x, float y) {
        setPosition(new Vector2f(x, y));
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension.setSize(dimension);
        if (dimensionChangeHandler != null) {
            dimensionChangeHandler.setComponent(this);
            dimensionChangeHandler.run();
        }
    }
    
    public void setDimension(int width, int height) {
        setDimension(new Dimension(width, height));
    }
    
    public GUICallback getPositionChangeHandler() {
        return positionChangeHandler;
    }

    public void setPositionChangeHandler(GUICallback positionChangeHandler) {
        this.positionChangeHandler = positionChangeHandler;
    }

    public GUICallback getDimensionChangeHandler() {
        return dimensionChangeHandler;
    }

    public void setDimensionChangeHandler(GUICallback dimensionChangeHandler) {
        this.dimensionChangeHandler = dimensionChangeHandler;
    }
    
    public boolean checkMouseIntersect(boolean checkParentContainerBox, boolean checkContainerBox, Shape... boxes) {
        boolean intersect1 = true, intersect2 = true;
        Shape mouseBox = Client.client.getMouseHitbox();
        if (checkContainerBox) {
            Shape cBox = getContainerBox();
            if (cBox != null) intersect1 = cBox.intersects(mouseBox);
        }
        if (intersect1 && checkParentContainerBox && parent != null) intersect2 = parent.checkMouseIntersect(checkParentContainerBox, true);
        if (!intersect1 || !intersect2) return false;
        List<GUI> guiList;
        if (parent != null && parent instanceof GUIContainer) guiList = ((GUIContainer)parent).getAllComponents();
        else guiList = Client.client.getGUIList();
        int thisIndex = guiList.indexOf(this);
        if (thisIndex > -1) {
            for (int i = 0; i < guiList.size(); i++) {
                if (i <= thisIndex) continue;
                GUI gui = guiList.get(i);
                if (gui.getComponentBox().intersects(mouseBox)) return false;
            }
        }
        if (boxes.length > 1) {
            for (Shape box : boxes) {
                if (!box.intersects(mouseBox)) return false;
            }
            return true;
        }
        if (boxes.length < 1) return true;
        return boxes[0].intersects(mouseBox);
    }
    
    public boolean checkMouseIntersect(boolean checkParentContainerBox, Shape... boxes) {
        return checkMouseIntersect(checkParentContainerBox, false, boxes);
    }
    
    public boolean checkMouseIntersect(Shape... boxes) {
        return checkMouseIntersect(true, false, boxes);
    }
    
    public Shape getContainerBox() {
        return null;
    }
    
    public Shape getComponentBox() {
        return new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
    }

    public GUI getParent() {
        return parent;
    }

    public void setParent(GUI parent) {
        this.parent = parent;
    }

    public GUIAlignment getParentAlignment() {
        return parentAlign;
    }

    public void setParentAlignment(GUIAlignment parentAlign) {
        this.parentAlign = parentAlign;
    }
    
    public void remove() {
        this.removeRequested = true;
    }

    public boolean isRemoveRequested() {
        return removeRequested;
    }
}