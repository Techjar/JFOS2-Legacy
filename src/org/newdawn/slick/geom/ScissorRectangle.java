package org.newdawn.slick.geom;

/**
 *
 * @author Techjar
 */
public class ScissorRectangle extends Rectangle {
    public ScissorRectangle(float x, float y, float width, float height) {
        super(x, y, width, height);
    }
    
    public ScissorRectangle(Rectangle rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    @Override
    public boolean contains(float xp, float yp) {
        if (xp < getX()) {
            return false;
        }
        if (yp < getY()) {
            return false;
        }
        if (xp > maxX+1) {
            return false;
        }
        if (yp > maxY+1) {
            return false;
        }

        return true;
    }
}
