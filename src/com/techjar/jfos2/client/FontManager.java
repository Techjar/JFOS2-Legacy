/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.techjar.jfos2.client;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    protected Map<Integer, UnicodeFont> fonts;
    
    public FontManager() {
        fontPath = new File("resources/fonts/");
        fonts = new HashMap<Integer, UnicodeFont>();
    }
    
    public FontInstance getFont(String font, int size, boolean bold, boolean italic, List<Effect> effects) {
        try {
            FontInfo info = new FontInfo(font, size, bold, italic, (effects == null || effects.isEmpty() ? 0 : effects.hashCode()));
            if (fonts.containsKey(info.hashCode())) return new FontInstance(fonts.get(info.hashCode()), info.hashCode());
            UnicodeFont unicodeFont = new UnicodeFont(new File(fontPath, font + ".ttf").getPath(), size, bold, italic);
            unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
            if (effects != null && !effects.isEmpty())
                unicodeFont.getEffects().addAll(effects);
            unicodeFont.addAsciiGlyphs();
            unicodeFont.loadGlyphs();
            fonts.put(info.hashCode(), unicodeFont);
            return new FontInstance(unicodeFont, info.hashCode());
        } catch (SlickException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public FontInstance getFont(String font, int size, boolean bold, boolean italic) {
        return getFont(font, size, bold, italic, null);
    }
    
    public UnicodeFont getFont(int identifier) {
        if (fonts.containsKey(identifier)) return fonts.get(identifier);
        return null;
    }
    
    public void unloadFont(int identifier) {
        if (fonts.containsKey(identifier)) fonts.remove(identifier).destroy();
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
        private int effects;

        public FontInfo(String font, int size, boolean bold, boolean italic, int effects) {
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

        public int getEffects() {
            return effects;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FontInfo other = (FontInfo) obj;
            if ((this.font == null) ? (other.font != null) : !this.font.equals(other.font)) {
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
            if (this.effects != other.effects) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + (this.font != null ? this.font.hashCode() : 0);
            hash = 89 * hash + this.size;
            hash = 89 * hash + (this.bold ? 1 : 0);
            hash = 89 * hash + (this.italic ? 1 : 0);
            hash = 89 * hash + this.effects;
            return hash;
        }

        @Override
        public String toString() {
            return "FontInfo{" + "font=" + font + ", size=" + size + ", bold=" + bold + ", italic=" + italic + ", effects=" + effects + '}';
        }
    }
    
    public class FontInstance {
        private UnicodeFont unicodeFont;
        private int identifier;

        public FontInstance(UnicodeFont unicodeFont, int identifier) {
            this.unicodeFont = unicodeFont;
            this.identifier = identifier;
        }

        public UnicodeFont getUnicodeFont() {
            return unicodeFont;
        }
        
        public int getIdentifier() {
            return identifier;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FontInstance other = (FontInstance) obj;
            if (this.unicodeFont != other.unicodeFont && (this.unicodeFont == null || !this.unicodeFont.equals(other.unicodeFont))) {
                return false;
            }
            if (this.identifier != other.identifier) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.unicodeFont != null ? this.unicodeFont.hashCode() : 0);
            hash = 17 * hash + this.identifier;
            return hash;
        }

        @Override
        public String toString() {
            return "FontInstance{" + "unicodeFont=" + unicodeFont + ", identifier=" + identifier + '}';
        }
    }
}
