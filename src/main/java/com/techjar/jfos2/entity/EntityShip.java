/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.entity;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.util.Util;
import org.lwjgl.input.Controller;
import org.newdawn.slick.opengl.Texture;

/**
 * @author Techjar
 */
public class EntityShip extends EntityFriendly {
	public EntityShip() {
		super();
		boundingBox = Util.loadShape("ship");
	}

	public EntityShip(int id) {
		super(id);
		boundingBox = Util.loadShape("ship");
	}

	@Override
	public void update(float delta) {
		super.update(delta);
	}

	@Override
	public void updateClient(float delta) {
		super.updateClient(delta);
	}

	@Override
	public void updateServer(float delta) {
		super.updateServer(delta);
	}

	@Override
	public void render() {
		super.render();
		Texture tex = Client.getInstance().getTextureManager().getTexture("ship.png");
		tex.bind();

		glPushMatrix();
		glTranslatef(position.getX(), position.getY(), 0);
		glRotatef(angle, 0, 0, 1);
		glTranslatef(-(tex.getImageWidth() / 2), -(tex.getImageHeight() / 2), 0);
		glColor3f(1, 1, 1);
		glBegin(GL_QUADS);
		glTexCoord2f(0, 0);
		glVertex2f(0, 0);
		glTexCoord2f(tex.getWidth(), 0);
		glVertex2f(tex.getImageWidth(), 0);
		glTexCoord2f(tex.getWidth(), tex.getHeight());
		glVertex2f(tex.getImageWidth(), tex.getImageHeight());
		glTexCoord2f(0, tex.getHeight());
		glVertex2f(0, tex.getImageHeight());
		glEnd();
		glPopMatrix();
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
