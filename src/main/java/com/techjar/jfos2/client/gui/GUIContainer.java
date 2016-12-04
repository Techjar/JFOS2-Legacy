package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.client.RenderHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Controller;
import org.newdawn.slick.geom.Rectangle;

/**
 * @author Techjar
 */
public abstract class GUIContainer extends GUI {
	protected List<GUI> components;

	public GUIContainer() {
		components = new ArrayList<>();
	}

	@Override
	public boolean processKeyboardEvent() {
		for (GUI gui : components)
			if (gui.isVisible() && gui.isEnabled() && !gui.processKeyboardEvent()) return false;
		return true;
	}

	@Override
	public boolean processMouseEvent() {
		for (GUI gui : components)
			if (gui.isVisible() && gui.isEnabled() && !gui.processMouseEvent()) return false;
		return true;
	}

	@Override
	public boolean processControllerEvent(Controller controller) {
		for (GUI gui : components)
			if (gui.isVisible() && gui.isEnabled() && !gui.processControllerEvent(controller)) return false;
		return true;
	}

	@Override
	public void update(float delta) {
		GUIWindow lastWin = null, lastTopWin = null;
		List<GUI> toAdd = new ArrayList<>();
		Iterator<GUI> it = components.iterator();
		while (it.hasNext()) {
			GUI gui = it.next();
			if (gui.isRemoveRequested()) it.remove();
			else {
				if (gui.isVisible() && gui.isEnabled()) {
					gui.update(delta);
					if (gui.isRemoveRequested()) it.remove();
					else if (gui instanceof GUIWindow) {
						GUIWindow win = (GUIWindow) gui;
						if (lastWin != null && lastWin != lastTopWin) lastWin.setOnTop(false);
						lastWin = win;
						if (win.isToBePutOnTop()) {
							it.remove();
							toAdd.add(gui);
							win.setOnTop(true);
							win.setToBePutOnTop(false);
							if (lastTopWin != null) lastTopWin.setOnTop(false);
							lastTopWin = win;
						}
					}
				}
			}
		}
		components.addAll(toAdd);
	}

	@Override
	public void render() {
		RenderHelper.beginScissor(getScissorBox());
		for (GUI gui : components) {
			if (gui.isVisible()) gui.render();
		}
		RenderHelper.endScissor();
	}

	@Override
	public void remove() {
		this.removeAllComponents();
		super.remove();
	}

	@Override
	public Rectangle getContainerBox() {
		return new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
	}

	protected Rectangle getScissorBox() {
		return getContainerBox();
	}

	public boolean containsComponent(GUI component) {
		return components.contains(component);
	}

	public GUI getComponent(int index) {
		return components.get(index);
	}

	public List<GUI> getAllComponents() {
		return Collections.unmodifiableList(components);
	}

	public void addComponent(GUI component) {
		components.add(component);
		component.setParent(this);
	}

	public void removeComponent(GUI component) {
		components.remove(component);
		component.setParent(null);
	}

	public GUI removeComponent(int index) {
		GUI component = components.get(index);
		if (component != null) removeComponent(component);
		return component;
	}

	public void removeAllComponents() {
		Iterator it = components.iterator();
		while (it.hasNext()) {
			GUI gui = (GUI) it.next();
			gui.remove();
			gui.setParent(null);
			it.remove();
		}
	}
}
