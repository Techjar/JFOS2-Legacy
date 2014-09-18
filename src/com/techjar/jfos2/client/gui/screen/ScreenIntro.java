
package com.techjar.jfos2.client.gui.screen;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
import com.techjar.jfos2.client.gui.GUIContainer;
import com.techjar.jfos2.util.Util;
import com.techjar.jfos2.util.Vector2;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.Color;
import org.newdawn.slick.UnicodeFont;

/**
 *
 * @author Techjar
 */
public class ScreenIntro extends Screen {
    protected long titleStart;
    protected boolean titleScreenAdded;
    protected UnicodeFont font;
    protected Screen titleScreen;
    protected boolean started;
    protected String titleMusic;

    public ScreenIntro() {
        super();
        font = Client.getInstance().getFontManager().getFont("astronbo", 64, true, false).getUnicodeFont();
    }

    protected long getTimePassed() {
        return Util.milliTime() - titleStart;
    }

    @Override
    public void render() {
        super.render();
        if (!started) {
            titleStart = Util.milliTime();
            titleMusic = Client.getInstance().getSoundManager().playMusic("music/title.mp3", true);
            started = true;
        }
        DisplayMode displayMode = Client.getInstance().getDisplayMode();
        if (getTimePassed() > 12000) {
            titleScreen.setEnabled(true);
            this.remove();
        }
        if (getTimePassed() > 11000 && !titleScreenAdded) {
            titleScreen = new ScreenTitle();
            titleScreen.setEnabled(false);
            Client.getInstance().addScreen(0, titleScreen);
            Client.getInstance().setRenderBackground(true);
            titleScreenAdded = true;
        }
        if (getTimePassed() < 5400) {
            String str = "Techjar Presents";
            int width = font.getWidth(str), height = font.getHeight(str);
            Vector2 fontCenter = Client.getInstance().getScreenCenter(width, height);
            font.drawString(fontCenter.getX(), fontCenter.getY(), str, org.newdawn.slick.Color.white);
        }
        if (getTimePassed() > 6400 && getTimePassed() < 10500) {
            String str = "Yet another space shooter...";
            int width = font.getWidth(str), height = font.getHeight(str);
            Vector2 fontCenter = Client.getInstance().getScreenCenter(width, height);
            font.drawString(fontCenter.getX(), fontCenter.getY(), str, org.newdawn.slick.Color.white);
        }
        if (getTimePassed() > 11000 && getTimePassed() < 12000) {
            RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * (1 - ((float)(getTimePassed() - 11000) / 1000f)))));
        }
        if (getTimePassed() > 9500 && getTimePassed() < 10500) {
            RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * ((float)(getTimePassed() - 9500) / 1000f))));
        }
        if (getTimePassed() > 6400 && getTimePassed() < 7400) {
            RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * (1 - ((float)(getTimePassed() - 6400) / 1000f)))));
        }
        if (getTimePassed() > 4400 && getTimePassed() < 5400) {
            RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * ((float)(getTimePassed() - 4400) / 1000f))));
        }
        if (getTimePassed() < 1000) {
            RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * (1 - ((float)getTimePassed() / 1000f)))));
        }
    }
}
