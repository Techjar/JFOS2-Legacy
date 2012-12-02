package com.techjar.jfos2.client.gui;

import static org.lwjgl.opengl.GL11.*;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
import java.awt.Cursor;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public class GUIWindow extends GUIContainer {
    protected GUIBackground guiBg;
    protected GUIButton closeBtn;
    protected Dimension minSize = new Dimension(50, 50);
    protected Dimension maxSize = new Dimension();
    protected boolean canMove = true;
    protected boolean canResize = true;
    
    protected Vector2f mouseLast;
    protected Cursor currentCursor;
    protected boolean dragging;
    protected boolean startResize;
    protected boolean mouseLockX, mouseLockY;
    protected int resizeX;
    protected int resizeY;

    public GUIWindow(GUIBackground guiBg) {
        this.guiBg = guiBg;
        this.guiBg.setParent(this);
        this.closeBtn = new GUIButton(new Color(0, 0, 0), Client.client.getTextureManager().getTexture("ui/windowclose.png"));
        this.closeBtn.setDimension(20, 20);
        this.closeBtn.setParent(this);
        this.closeBtn.windowClose = true;
        this.closeBtn.setClickHandler(new GUICallback(this) {
            @Override
            public void run() {
                Object[] args = this.getArgs();
                if (args.length < 1 || !(args[0] instanceof GUIWindow)) return;
                GUIWindow window = (GUIWindow)args[0];
                window.remove();
            }
        });
    }

    @Override
    public boolean processKeyboardEvent() {
        return super.processKeyboardEvent();
    }

    @Override
    public boolean processMouseEvent() {
        if (!super.processMouseEvent()) return false;
        closeBtn.processMouseEvent();
        if (Mouse.getEventButton() == 0) {
            if (Mouse.getEventButtonState()) {
                if (canResize) {
                    Rectangle[] boxes = getBoxes();
                    if (checkMouseIntersect(boxes[0])) {
                        resizeY = -1;
                        if (checkMouseIntersect(boxes[1])) {
                            resizeX = -1;
                        }
                        else if (checkMouseIntersect(boxes[3])) {
                            resizeX = 1;
                        }
                    }
                    else if (checkMouseIntersect(boxes[2])) {
                        resizeY = 1;
                        if (checkMouseIntersect(boxes[1])) {
                            resizeX = -1;
                        }
                        else if (checkMouseIntersect(boxes[3])) {
                            resizeX = 1;
                        }
                    }
                    else if (checkMouseIntersect(boxes[1])) {
                        resizeX = -1;
                    }
                    else if (checkMouseIntersect(boxes[3])) {
                        resizeX = 1;
                    }
                    if (isResizing()) startResize = true;
                }
                if (!isResizing() && canMove) {
                    Rectangle head = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), 20);
                    if (checkMouseIntersect(head)) dragging = true;
                }
                if (dragging || isResizing()) {
                    mouseLast = Client.client.getMousePos();
                    return false;
                }
            }
            else {
                dragging = false;
                resizeX = 0;
                resizeY = 0;
            }
        }
        return true;
    }

    @Override
    public void render() {
        guiBg.render();
        RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), 20, guiBg.getBorderColor());
        closeBtn.render();
        super.render();
    }

    @Override
    public void update() {
        super.update();
        closeBtn.update();
        if (canResize && !Client.client.getMousePos().equals(mouseLast)) {
            if (isResizing()) {
                Vector2f mouseDiff = Vector2f.sub(Client.client.getMousePos(), mouseLast, null);
                Vector2f newPos = new Vector2f(position);
                Dimension newDim = new Dimension(dimension);
                if (resizeX == 1) {
                    newDim.setWidth(dimension.getWidth() + (int)mouseDiff.getX());
                }
                else if (resizeX == -1) {
                    newPos.setX(position.getX() + mouseDiff.getX());
                    newDim.setWidth(dimension.getWidth() - (int)mouseDiff.getX());
                }
                if (resizeY == 1) {
                    newDim.setHeight(dimension.getHeight() + (int)mouseDiff.getY());
                }
                else if (resizeY == -1) {
                    newPos.setY(position.getY() + mouseDiff.getY());
                    newDim.setHeight(dimension.getHeight() - (int)mouseDiff.getY());
                }
                if (newDim.getWidth() > minSize.getWidth() && (maxSize.getWidth() == 0 || newDim.getWidth() < maxSize.getWidth())) {
                    if (newDim.getWidth() != dimension.getWidth()) setDimension(newDim.getWidth(), dimension.getHeight());
                    if (newPos.getX() != position.getX()) setPosition(newPos.getX(), position.getY());
                    mouseLockX = false;
                }
                else mouseLockX = true;
                if (newDim.getHeight() > minSize.getHeight() && (maxSize.getHeight() == 0 || newDim.getHeight() < maxSize.getHeight())) {
                    if (newDim.getHeight() != dimension.getHeight()) setDimension(dimension.getWidth(), newDim.getHeight());
                    if (newPos.getY() != position.getY()) setPosition(position.getX(), newPos.getY());
                    mouseLockY = false;
                }
                else mouseLockY = true;
            }
            
            if (!Mouse.isButtonDown(0) || startResize) {
                startResize = false;
                Rectangle[] boxes = getBoxes();
                if (checkMouseIntersect(boxes[0])) {
                    if (checkMouseIntersect(boxes[1])) {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                    }
                    else if (checkMouseIntersect(boxes[3])) {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                    }
                    else {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                    }
                }
                else if (checkMouseIntersect(boxes[2])) {
                    if (checkMouseIntersect(boxes[1])) {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                    }
                    else if (checkMouseIntersect(boxes[3])) {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                    }
                    else {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                    }
                }
                else if (checkMouseIntersect(boxes[1])) {
                    currentCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                }
                else if (checkMouseIntersect(boxes[3])) {
                    currentCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                }
                else {
                    currentCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                }
            }
            Client.client.getFrame().setCursor(currentCursor);
        }
        if (dragging) {
            setPosition(Vector2f.add(position, Vector2f.sub(Client.client.getMousePos(), mouseLast, null), null));
        }
        if (mouseLockX && !mouseLockY) mouseLast.setY(Client.client.getMouseY());
        else if (!mouseLockX && mouseLockY) mouseLast.setX(Client.client.getMouseX());
        else if (!mouseLockX && !mouseLockY) mouseLast = Client.client.getMousePos();
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        guiBg.setDimension(dimension);
        closeBtn.setPosition(dimension.getWidth() - 20, 0);
    }

    @Override
    public Rectangle getContainerBox() {
        return new Rectangle(getPosition().getX() + 2, getPosition().getY() + 20, dimension.getWidth() - 4, dimension.getHeight() - 22);
    }

    public boolean isMoveable() {
        return canMove;
    }

    public void setMoveable(boolean canMove) {
        this.canMove = canMove;
    }

    public boolean isResizable() {
        return canResize;
    }

    public void setResizable(boolean canResize) {
        this.canResize = canResize;
    }
    
    public Dimension getMinimumSize() {
        return minSize;
    }

    public void setMinimumSize(Dimension minSize) {
        this.minSize = minSize;
    }

    public Dimension getMaximumSize() {
        return maxSize;
    }

    public void setMaximumSize(Dimension maxSize) {
        this.maxSize = maxSize;
    }
    
    protected boolean isResizing() {
        return resizeX != 0 || resizeY != 0;
    }
    
    protected Rectangle[] getBoxes() {
        Rectangle[] boxes = new Rectangle[4];
        boxes[0] = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), guiBg.getBorderSize());
        boxes[1] = new Rectangle(getPosition().getX(), getPosition().getY(), guiBg.getBorderSize(), dimension.getHeight());
        boxes[2] = new Rectangle(getPosition().getX(), getPosition().getY() + (dimension.getHeight() - guiBg.getBorderSize()), dimension.getWidth(), guiBg.getBorderSize());
        boxes[3] = new Rectangle(getPosition().getX() + (dimension.getWidth() - guiBg.getBorderSize()), getPosition().getY(), guiBg.getBorderSize(), dimension.getHeight());
        return boxes;
    }
}
