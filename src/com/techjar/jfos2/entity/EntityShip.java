/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.entity;

import static org.lwjgl.opengl.GL11.*;

import com.techjar.jfos2.client.Client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.lwjgl.input.Controller;

/**
 *
 * @author Techjar
 */
public class EntityShip extends EntityFriendly {
    public EntityShip(int id) {
        super(id);
        angle = 90;
    }

    @Override
    public void update(double delta) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateClient(double delta) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateServer(double delta) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void render() {
        Client.getInstance().getTextureManager().getTexture("ship.png").bind();
        glPushMatrix();
        glTranslatef(position.getX(), position.getY(), 0);
        glRotatef(angle, 0, 0, 1);
        glTranslatef(-(139 / 2), -(148 / 2), 0);
        glColor3f(1, 1, 1);
        glBegin(GL_QUADS);
            glTexCoord2f(0, 0); glVertex2f(0, 0);
            glTexCoord2f(0.54296875f, 0); glVertex2f(139, 0);
            glTexCoord2f(0.54296875f, 0.578125f); glVertex2f(139, 148);
            glTexCoord2f(0, 0.578125f); glVertex2f(0, 148);
        glEnd();
        glPopMatrix();
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        super.readData(stream);
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        super.writeData(stream);
    }

    @Override
    public boolean processControllerEvent(Controller controller) {
        return super.processControllerEvent(controller);
    }

    @Override
    public boolean processKeyboardEvent() {
        return super.processKeyboardEvent();
    }

    @Override
    public boolean processMouseEvent() {
        return super.processMouseEvent();
    }
}
