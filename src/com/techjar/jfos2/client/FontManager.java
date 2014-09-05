package com.techjar.jfos2.client;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.font.effects.Effect;

/**
 *
 * @author Techjar
 */
public class FontManager {
    protected final File fontPath;
    protected Map<FontInfo, UnicodeFont> fonts;
    
    public FontManager() {
        fontPath = new File("resources/fonts/");
        fonts = new HashMap<>();
    }
    
    public FontInstance getFont(String font, int size, boolean bold, boolean italic, List<Effect> effects) {
        FontInfo info = new FontInfo(font, size, bold, italic, effects);
        return new FontInstance(getFont(info), info);
    }
    
    public FontInstance getFont(String font, int size, boolean bold, boolean italic) {
        return getFont(font, size, bold, italic, null);
    }
    
    public UnicodeFont getFont(FontInfo info) {
        try {
            if (fonts.containsKey(info)) return fonts.get(info);
            UnicodeFont unicodeFont = new UnicodeFont(new File(fontPath, info.getFont() + ".ttf").getPath(), info.getSize(), info.isBold(), info.isItalic());
            unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
            if (info.getEffects() != null && !info.getEffects().isEmpty())
                unicodeFont.getEffects().addAll(info.getEffects());
            unicodeFont.addAsciiGlyphs();
            unicodeFont.loadGlyphs();
            fonts.put(info, unicodeFont);
            return unicodeFont;
        } catch (SlickException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void unloadFont(FontInfo info) {
        if (fonts.containsKey(info)) fonts.remove(info).destroy();
    }
    
    public void cleanup() {
        for (UnicodeFont font : fonts.values())
            font.destroy();
        fonts.clear();
    }
    
    public class FontInfo {
        private String font;
        private int size;
        private boolean bold;
        private boolean italic;
        private List<Effect> effects;

        public FontInfo(String font, int size, boolean bold, boolean italic, List<Effect> effects) {
            this.font = font;
            this.size = size;
            this.bold = bold;
            this.italic = italic;
            this.effects = effects;
        }

        public String getFont() {
            return font;
        }
        
        public int getSize() {
            return size;
        }
        
        public boolean isBold() {
            return bold;
        }

        public boolean isItalic() {
            return italic;
        }

        public List<Effect> getEffects() {
            return effects;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + Objects.hashCode(this.font);
            hash = 47 * hash + this.size;
            hash = 47 * hash + (this.bold ? 1 : 0);
            hash = 47 * hash + (this.italic ? 1 : 0);
            hash = 47 * hash + Objects.hashCode(this.effects);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FontInfo other = (FontInfo)obj;
            if (!Objects.equals(this.font, other.font)) {
                return false;
            }
            if (this.size != other.size) {
                return false;
            }
            if (this.bold != other.bold) {
                return false;
            }
            if (this.italic != other.italic) {
                return false;
            }
            if (!Objects.equals(this.effects, other.effects)) {
                return false;
            }
            return true;
        }
    }
    
    public class FontInstance {
        private UnicodeFont unicodeFont;
        private FontInfo info;

        public FontInstance(UnicodeFont unicodeFont, FontInfo info) {
            this.unicodeFont = unicodeFont;
            this.info = info;
        }

        public UnicodeFont getUnicodeFont() {
            return unicodeFont;
        }
        
        public FontInfo getInfo() {
            return info;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + Objects.hashCode(this.unicodeFont);
            hash = 41 * hash + Objects.hashCode(this.info);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FontInstance other = (FontInstance)obj;
            if (!Objects.equals(this.unicodeFont, other.unicodeFont)) {
                return false;
            }
            if (!Objects.equals(this.info, other.info)) {
                return false;
            }
            return true;
        }
    }
}
