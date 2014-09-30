
package com.techjar.jfos2.client.gui.screen;

import com.techjar.jfos2.client.Client;
import com.techjar.jfos2.client.RenderHelper;
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
            int width = font.getWidth(str);
            int height = font.getHeight(str);
            Vector2 fontCenter = Client.getInstance().getScreenCenter(width, height);
            float alpha = 1;
            if (getTimePassed() < 1000) alpha = getTimePassed() / 1000F;
            else if (getTimePassed() > 4400) alpha = 1 - (getTimePassed() - 4400) / 1000F;
            font.drawString(fontCenter.getX(), fontCenter.getY(), str, new org.newdawn.slick.Color(1.0F, 1.0F, 1.0F, alpha));
        }
        if (getTimePassed() > 6400 && getTimePassed() < 10500) {
            String str = "Yet another space shooter...";
            int width = font.getWidth(str);
            int height = font.getHeight(str);
            Vector2 fontCenter = Client.getInstance().getScreenCenter(width, height);
            float alpha = 1;
            if (getTimePassed() < 7400) alpha = (getTimePassed() - 6400) / 1000F;
            else if (getTimePassed() > 9500) alpha = 1 - (getTimePassed() - 9500) / 1000F;
            font.drawString(fontCenter.getX(), fontCenter.getY(), str, new org.newdawn.slick.Color(1.0F, 1.0F, 1.0F, alpha));
        }
        if (getTimePassed() > 11000 && getTimePassed() < 12000) {
            RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * (1 - ((float)(getTimePassed() - 11000) / 1000f)))));
        }
    }
}
