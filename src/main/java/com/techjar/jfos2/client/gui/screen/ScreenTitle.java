
package com.techjar.jfos2.client.gui.screen;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.gui.GUIAlignment;
import com.techjar.jfos2.client.gui.GUILabel;
import com.techjar.jfos2.util.Constants;

import java.util.ArrayList;

import org.lwjgl.util.Color;
import org.newdawn.slick.font.effects.Effect;
import org.newdawn.slick.font.effects.GradientEffect;
import org.newdawn.slick.font.effects.OutlineEffect;

/**
 * @author Techjar
 */
public class ScreenTitle extends Screen {
	public ScreenTitle() {
		super();
		ArrayList<Effect> titleEffects = new ArrayList<>();
		titleEffects.add(new GradientEffect(java.awt.Color.WHITE, java.awt.Color.BLACK, 1));
		titleEffects.add(new OutlineEffect(2, java.awt.Color.LIGHT_GRAY));
		GUILabel title = new GUILabel(Client.getInstance().getFontManager().getFont("Data", 48, false, false, titleEffects).getUnicodeFont(), new Color(0, 200, 0), Constants.GAME_TITLE);
		title.setParentAlignment(GUIAlignment.TOP_CENTER);
		title.setDimension(title.getFont().getWidth(title.getText()), title.getFont().getHeight(title.getText()));
		title.setPosition(0, 40);
		container.addComponent(title);
	}
}
