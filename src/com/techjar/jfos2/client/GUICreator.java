package com.techjar.jfos2.client;

import com.techjar.jfos2.client.gui.*;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.Color;
import org.newdawn.slick.font.effects.*;

/**
 *
 * @author Techjar
 */
public class GUICreator {
    public static void setupTitleScreen(final Client client) {
        client.setRenderBackground(true);
        final GUIBox box = new GUIBox(null);
        box.setDimension(client.getWidth(), client.getHeight());
        final int rhIndex = client.addResizeHandler(new GUICallback() {
            @Override
            public void run() {
                box.setDimension(client.getWidth(), client.getHeight());
            }
        });
        box.setRemoveHandler(new GUICallback() {
            @Override
            public void run() {
                client.removeResizeHandler(rhIndex);
            }
        });
        List<Effect> titleEffects = new ArrayList<>();
        titleEffects.add(new GradientEffect(java.awt.Color.WHITE, java.awt.Color.BLACK, 1));
        titleEffects.add(new OutlineEffect(2, java.awt.Color.LIGHT_GRAY));
        GUILabel title = new GUILabel(client.getFontManager().getFont("Data", 48, false, false, titleEffects).getUnicodeFont(), new Color(0, 200, 0), "Junk from Outer Space 2");
        title.setParentAlignment(GUIAlignment.TOP_CENTER);
        title.setDimension(title.getFont().getWidth(title.getText()), title.getFont().getHeight(title.getText()));
        title.setPosition(0, 40);
        box.addComponent(title);
        /*GUIInputOption thing2 = new GUIInputOption(client.getFontManager().getFont("COPRGTB", 24, false, false).getUnicodeFont(), new Color(200, 0, 0));
        thing2.setPosition(50, 50);
        thing2.setDimension(200, 24);
        box.addComponent(thing2);*/
        //client.addGUI(box);
    }
}
