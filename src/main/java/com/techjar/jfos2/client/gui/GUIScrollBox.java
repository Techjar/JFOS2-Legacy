package com.techjar.jfos2.client.gui;

import com.techjar.jfos2.util.MathHelper;
import com.techjar.jfos2.util.Util;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
import com.techjar.jfos2.util.Vector2;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.newdawn.slick.geom.Rectangle;

/**
 * @author Techjar
 */
public class GUIScrollBox extends GUIContainer {
	protected Color color;
	protected ScrollMode scrollXMode = ScrollMode.AUTOMATIC;
	protected ScrollMode scrollYMode = ScrollMode.AUTOMATIC;
	protected int scrollXIncrement;
	protected int scrollYIncrement;

	protected Vector2 scrollOffset = new Vector2();
	protected Vector2 scrollOffsetStart = new Vector2();
	protected Vector2 scrollbarOffsetStart = new Vector2();
	protected Vector2 mouseStart = new Vector2();
	protected int scrolling;

	public GUIScrollBox(Color color) {
		this.color = color;
	}

	@Override
	public boolean processKeyboardEvent() {
		return super.processKeyboardEvent();
	}

	@Override
	public boolean processMouseEvent() {
		if (!super.processMouseEvent()) return false;
		if (Mouse.getEventButton() == 0) {
			if (Mouse.getEventButtonState()) {
				Vector2 scrollbarOffset = getScrollbarOffset();
				float[] sizeFactor = getScrollbarSizeFactor();
				if (getScrollX()) {
					Rectangle box = new Rectangle(getPosition().getX() + scrollbarOffset.getX() + 1, getPosition().getY() + dimension.getHeight() - 9, (int) ((dimension.getWidth() - 2) * sizeFactor[0]) - (getScrollX() && getScrollY() ? 10 : 0), 8);
					if (checkMouseIntersect(box)) {
						scrolling = 1;
						mouseStart.set(Client.getInstance().getMousePos());
						scrollOffsetStart.set(scrollOffset);
						scrollbarOffsetStart.set(getScrollbarOffset());
						return false;
					}
				}
				if (getScrollY()) {
					Rectangle box = new Rectangle(getPosition().getX() + dimension.getWidth() - 9, getPosition().getY() + scrollbarOffset.getY() + 1, 8, (int) ((dimension.getHeight() - 2) * sizeFactor[1]) - (getScrollX() && getScrollY() ? 10 : 0));
					if (checkMouseIntersect(box)) {
						scrolling = 2;
						mouseStart.set(Client.getInstance().getMousePos());
						scrollOffsetStart.set(scrollOffset);
						scrollbarOffsetStart.set(getScrollbarOffset());
						return false;
					}
				}
			} else scrolling = 0;
		}
		if (scrolling == 0 && Mouse.getEventDWheel() != 0) {
			if (checkMouseIntersect(getComponentBox())) {
				int[] maxScrollOffset = getMaxScrollOffset();
				if (scrollYIncrement > 0)
					scrollOffset.setY(MathHelper.clamp(scrollOffset.getY() + (scrollYIncrement * -MathHelper.sign(Mouse.getEventDWheel())), 0, maxScrollOffset[1]));
				else
					scrollOffset.setY(MathHelper.clamp(scrollOffset.getY() - Mouse.getEventDWheel(), 0, maxScrollOffset[1]));
				return false;
			}
		}
		return true;
	}

	@Override
	public void update(float delta) {
		if (scrolling != 0) {
			Vector2 mouseOffset = Client.getInstance().getMousePos().subtract(mouseStart);
			int[] maxScrollOffset = getMaxScrollOffset();
			int[] maxScrollbarOffset = getMaxScrollbarOffset();
			if (scrolling == 1 && maxScrollOffset[0] > 0) {
				float offset = MathHelper.clamp(scrollOffsetStart.getX() + (mouseOffset.getX() * ((float) maxScrollOffset[0] / (float) maxScrollbarOffset[0])), 0, maxScrollOffset[0]);
				if (scrollXIncrement > 0) scrollOffset.setX(offset - (offset % scrollXIncrement));
				else scrollOffset.setX(offset);
			} else if (scrolling == 2 && maxScrollOffset[1] > 0) {
				float offset = MathHelper.clamp(scrollOffsetStart.getY() + (mouseOffset.getY() * ((float) maxScrollOffset[1] / (float) maxScrollbarOffset[1])), 0, maxScrollOffset[1]);
				if (scrollYIncrement > 0) scrollOffset.setY(offset - (offset % scrollYIncrement));
				else scrollOffset.setY(offset);
			}
		}
		super.update(delta);
	}

	@Override
	public void render() {
		Vector2 scrollbarOffset = getScrollbarOffset();
		float[] sizeFactor = getScrollbarSizeFactor();
		if (getScrollX()) {
			Color color2 = new Color(color);
			Rectangle box = new Rectangle(getPosition().getX() + scrollbarOffset.getX() + 1, getPosition().getY() + dimension.getHeight() - 9, (int) ((dimension.getWidth() - 2) * sizeFactor[0]) - (getScrollX() && getScrollY() ? 10 : 0), 8);
			if (scrolling == 1 || (scrolling == 0 && checkMouseIntersect(box))) {
				color2 = Util.addColors(color2, new Color(50, 50, 50));
			}
			RenderHelper.drawSquare(getPosition().getX() + scrollbarOffset.getX() + 1, getPosition().getY() + dimension.getHeight() - 9, (int) ((dimension.getWidth() - 2) * sizeFactor[0]) - (getScrollX() && getScrollY() ? 10 : 0), 8, color2);
		}
		if (getScrollY()) {
			Color color2 = new Color(color);
			Rectangle box = new Rectangle(getPosition().getX() + dimension.getWidth() - 9, getPosition().getY() + scrollbarOffset.getY() + 1, 8, (int) ((dimension.getHeight() - 2) * sizeFactor[1]) - (getScrollX() && getScrollY() ? 10 : 0));
			if (scrolling == 2 || (scrolling == 0 && checkMouseIntersect(box))) {
				color2 = Util.addColors(color2, new Color(50, 50, 50));
			}
			RenderHelper.drawSquare(getPosition().getX() + dimension.getWidth() - 9, getPosition().getY() + scrollbarOffset.getY() + 1, 8, (int) ((dimension.getHeight() - 2) * sizeFactor[1]) - (getScrollX() && getScrollY() ? 10 : 0), color2);
		}
		if (getScrollX() && getScrollY()) {
			RenderHelper.drawSquare(getPosition().getX() + dimension.getWidth() - 9, getPosition().getY() + dimension.getHeight() - 9, 8, 8, color);
		}
		super.render();
	}

	@Override
	public Vector2 getContainerPosition() {
		return getPosition().subtract(scrollOffset);
	}

	@Override
	public Rectangle getContainerBox() {
		return new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth() - (getScrollY() ? 10 : 0), dimension.getHeight() - (getScrollX() ? 10 : 0));
	}

	private boolean getScrollX(boolean checkAuto) {
		if (checkAuto && scrollXMode == ScrollMode.AUTOMATIC) return getMaxScrollOffset()[0] > 0;
		return scrollXMode == ScrollMode.ENABLED || scrollXMode == ScrollMode.AUTOMATIC;
	}

	private boolean getScrollX() {
		return getScrollX(true);
	}

	private boolean getScrollY(boolean checkAuto) {
		if (checkAuto && scrollYMode == ScrollMode.AUTOMATIC) return getMaxScrollOffset()[1] > 0;
		return scrollYMode == ScrollMode.ENABLED || scrollYMode == ScrollMode.AUTOMATIC;
	}

	private boolean getScrollY() {
		return getScrollY(true);
	}

	public ScrollMode getScrollXMode() {
		return scrollXMode;
	}

	public void setScrollXMode(ScrollMode scrollXMode) {
		if (scrollXMode == null) throw new NullPointerException();
		this.scrollXMode = scrollXMode;
	}

	public ScrollMode getScrollYMode() {
		return scrollYMode;
	}

	public void setScrollYMode(ScrollMode scrollYMode) {
		if (scrollYMode == null) throw new NullPointerException();
		this.scrollYMode = scrollYMode;
	}

	public int getScrollXIncrement() {
		return scrollXIncrement;
	}

	public void setScrollXIncrement(int scrollXIncrement) {
		this.scrollXIncrement = scrollXIncrement;
	}

	public int getScrollYIncrement() {
		return scrollYIncrement;
	}

	public void setScrollYIncrement(int scrollYIncrement) {
		this.scrollYIncrement = scrollYIncrement;
	}

	public Vector2 getScrollOffset() {
		return scrollOffset.copy();
	}

	public void setScrollOffset(Vector2 offset) {
		int[] maxOffset = getMaxScrollOffset();
		this.scrollOffset = new Vector2(MathHelper.clamp(offset.getX(), 0, maxOffset[0]), MathHelper.clamp(offset.getY(), 0, maxOffset[1]));
	}

	public void setScrollOffset(int x, int y) {
		setScrollOffset(new Vector2(x, y));
	}

	public Vector2 getScrollbarOffset() {
		int[] maxOffset = getMaxScrollOffset();
		int[] maxBarOffset = getMaxScrollbarOffset();
		return new Vector2((maxOffset[0] == 0 ? 0 : maxBarOffset[0] * (scrollOffset.getX() / (float) maxOffset[0])), (maxOffset[1] == 0 ? 0 : maxBarOffset[1] * (scrollOffset.getY() / (float) maxOffset[1])));
	}

	public int[] getMaxScrollOffset() {
		int[] maxOffset = new int[2];
		GUI bottom = getBottomComponent(), right = getRightComponent();
		if (getScrollY(false) && bottom != null) {
			maxOffset[1] = (int) MathHelper.clamp(bottom.getRawPosition().getY() + bottom.getDimension().getHeight() - dimension.getHeight() + (getScrollX(false) ? 10 : 0), 0, Integer.MAX_VALUE);
		}
		if (getScrollX(false) && right != null) {
			maxOffset[0] = (int) MathHelper.clamp(bottom.getRawPosition().getX() + bottom.getDimension().getWidth() - dimension.getWidth() + (getScrollY(false) ? 10 : 0), 0, Integer.MAX_VALUE);
		}
		return maxOffset;
	}

	public int[] getMaxScrollbarOffset() {
		float[] sizeFactor = getScrollbarSizeFactorInverse();
		int[] offset = new int[2];
		offset[0] = (int) (dimension.getWidth() * sizeFactor[0]);
		offset[1] = (int) (dimension.getHeight() * sizeFactor[1]);
		return offset;
	}

	public float[] getScrollbarSizeFactor() {
		float[] size = new float[]{1, 1};
		GUI bottom = getBottomComponent(), right = getRightComponent();
		if (getScrollY(false) && bottom != null) {
			float compPos = bottom.getRawPosition().getY() + bottom.getDimension().getHeight();
			if (compPos > 0 && compPos >= dimension.getHeight() - (getScrollX(false) ? 10 : 0))
				size[1] = (float) (dimension.getHeight() - (getScrollX(false) ? 10 : 0)) / compPos;
		}
		if (getScrollX(false) && right != null) {
			float compPos = right.getRawPosition().getX() + right.getDimension().getWidth();
			if (compPos > 0 && compPos >= dimension.getWidth() - (getScrollY(false) ? 10 : 0))
				size[0] = (float) (dimension.getWidth() - (getScrollY(false) ? 10 : 0)) / compPos;
		}
		return size;
	}

	public float[] getScrollbarSizeFactorInverse() {
		float[] size = getScrollbarSizeFactor();
		size[0] = 1 - size[0];
		size[1] = 1 - size[1];
		return size;
	}

	public GUI getBottomComponent() {
		GUI bottom = null;
		for (GUI gui : components) {
			if (bottom == null) bottom = gui;
			else if (gui.getRawPosition().getY() > bottom.getRawPosition().getY()) bottom = gui;
		}
		return bottom;
	}

	public GUI getRightComponent() {
		GUI right = null;
		for (GUI gui : components) {
			if (right == null) right = gui;
			else if (gui.getRawPosition().getX() > right.getRawPosition().getX()) right = gui;
		}
		return right;
	}

	public static enum ScrollMode {
		ENABLED,
		DISABLED,
		AUTOMATIC
	}
}
