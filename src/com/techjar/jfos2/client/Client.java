package com.techjar.jfos2.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.util.glu.GLU.*;

import com.techjar.jfos2.ConfigManager;
import com.techjar.jfos2.Constants;
import org.lwjgl.opengl.DisplayMode;
import com.techjar.jfos2.OperatingSystem;
import com.techjar.jfos2.TickCounter;
import com.techjar.jfos2.client.gui.*;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public class Client {
    public static Client client;
    private final File dataDir;
    private Frame frame;
    private Canvas canvas;
    private Rectangle mouseHitbox;
    private ConfigManager config;
    private FontManager font;
    private TextureManager texture;
    private SoundManager sound;
    private DisplayMode displayMode;
    private List<DisplayMode> displayModeList;
    private PixelFormat pixelFormat;
    private TickCounter tick;
    private List<GUI> guiList;
    private AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
    private long fps;
    private long fpsLastFrame;
    private boolean closeRequested;
    private boolean arbSupported;
    private int arbMaxSamples;
    
    
    public Client() {
        dataDir = OperatingSystem.getDataDirectory("jfos2");
        mouseHitbox = new Rectangle(0, 0, 0, 0);
        tick = new TickCounter(Constants.FRAME_RATE);
        guiList = new ArrayList<GUI>();
    }
    
    public static void run(String[] args) {
        if (client != null) throw new RuntimeException("Client is already running");
        try {
            client = new Client();
            client.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                crashException(ex);
            }
            catch (Exception ex2) {
                System.exit(0); // Everything broke, bail!
            }
        }
    }
    
    public void start() throws LWJGLException, SlickException {
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        initDisplayModes();
        initConfig();
        
        PixelFormat format = new PixelFormat(32, 0, 24, 8, 0);
        Pbuffer pb = new Pbuffer(640, 480, format, null);
        pb.makeCurrent();
        arbSupported = GLContext.getCapabilities().GL_ARB_multisample;
        if(arbSupported) arbMaxSamples = glGetInteger(GL_MAX_SAMPLES);
        pb.destroy();
        
        frame = new Frame(Constants.GAME_TITLE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        canvas = new Canvas();

        /*canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                newCanvasSize.set(canvas.getSize());
            }
        });*/

        frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                canvas.requestFocusInWindow();
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeRequested = true;
            }
        });

        frame.add(canvas, BorderLayout.CENTER);
        Display.setParent(canvas);
        Display.setVSyncEnabled(true);
        Display.setDisplayMode(displayMode);
        
        canvas.setPreferredSize(new Dimension(displayMode.getWidth(), displayMode.getHeight()));
        frame.pack();
        frame.setVisible(true);
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((dim.width - frame.getSize().width) / 2, (dim.height - frame.getSize().height) / 2);
        
        Display.create();
        init();
        drawSplash();
        
        Keyboard.create();
        Mouse.create();
        Mouse.setGrabbed(false);
        font = new FontManager();
        texture = new TextureManager();
        sound = new SoundManager();
        preloadData();
        
        GUIWindow slider = new GUIWindow(new GUIBackground());
        slider.setDimension(500, 500);
        slider.setPosition(56, 50);
        /*slider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                GUISlider slid = (GUISlider)this.getComponent();
                sound.setMasterVolume(slid.getValue());
            }
        });*/
        guiList.add(slider);
        GUIContainer thing = new GUIScrollBox(new Color(100, 0, 0));
        thing.setDimension((int)slider.getContainerBox().getWidth(), (int)slider.getContainerBox().getHeight());
        thing.setPosition(2, 20);
        slider.addComponent(thing);
        GUIInputOption thing2 = new GUIInputOption(font.getFont("COPRGTB", 24, false, false).getUnicodeFont(), new Color(200, 0, 0));
        thing2.setDimension(200, 30);
        thing2.setPosition(40, 800);
        thing2.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                Client.client.setDisplayMode(new DisplayMode(800, 600));
            }
        });
        thing.addComponent(thing2);
        //sound.playStreamingSound("myass.mp3", false);
        
        
        run();
        shutdownInternal();
        System.exit(0);
    }

    public static void crashException(Throwable ex) {
        Display.destroy();
        if (client.canvas != null && client.frame != null) {
            client.frame.setResizable(false);
            client.frame.setSize(new Dimension(client.displayMode.getWidth(), client.displayMode.getHeight()));
            Graphics g = client.canvas.getGraphics();
            g.setFont(new java.awt.Font("Monospaced", Font.BOLD, 50));
            g.drawString(Constants.GAME_TITLE + " has crashed!", 55, 50);
        }
    }

    private void initDisplayModes() throws LWJGLException {
        displayModeList = new ArrayList<DisplayMode>();
        DisplayMode desktop = Display.getDesktopDisplayMode();
        for (DisplayMode mode : Display.getAvailableDisplayModes()) {
            if(mode.getBitsPerPixel() == desktop.getBitsPerPixel() && mode.getFrequency() == desktop.getFrequency()) {
                displayModeList.add(mode);
                if (mode.getWidth() == 1024 && mode.getHeight() == 768) displayMode = mode;
            }
        }
        Collections.sort(displayModeList, new ResolutionSorter());
    }

    private void initConfig() {
        config = new ConfigManager(new File(dataDir, "options.yml"));
        config.load();
        config.defaultProperty("display.width", displayMode.getWidth());
        config.defaultProperty("display.height", displayMode.getHeight());

        DisplayMode dMode = findDisplayMode((Integer)config.getProperty("display.width"), (Integer)config.getProperty("display.height"));
        if (dMode != null) displayMode = dMode;
        else {
            config.setProperty("display.width", displayMode.getWidth());
            config.setProperty("display.height", displayMode.getHeight());
        }
        
        config.save();
    }

    public DisplayMode findDisplayMode(int width, int height) {
        for (DisplayMode mode : displayModeList) {
            if(mode.getWidth() == width && mode.getHeight() == height) {
                return mode;
            }
        }
        return null;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        try {
            this.displayMode = displayMode;
            Display.setDisplayMode(displayMode);
            canvas.setPreferredSize(new Dimension(displayMode.getWidth(), displayMode.getHeight()));
            frame.pack();
            resizeGL(displayMode.getWidth(), displayMode.getHeight());
        }
        catch (LWJGLException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public void setDisplayMode(int width, int height) {
        DisplayMode mode = findDisplayMode(width, height);
        if (mode != null) setDisplayMode(mode);
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public List<DisplayMode> getDisplayModeList() {
        return Collections.unmodifiableList(displayModeList);
    }

    public List<String> getDisplayModeStrings() {
        List<String> list = new ArrayList<String>(displayModeList.size());
        for (DisplayMode mode : displayModeList) {
            list.add(new StringBuilder(mode.getWidth()).append("x").append(mode.getHeight()).toString());
        }
        return Collections.unmodifiableList(list);
    }

    private void preloadData() {
        // Preload Textures
        texture.getTexture("ui/windowclose.png");
        
        // Preload Sounds
        sound.loadSound("ui/click.wav");
        sound.loadSound("ui/rollover.wav");
    }
    
    private void drawSplash() {
        
    }
    
    private void init() {
        initGL();
        resizeGL(displayMode.getWidth(), displayMode.getHeight());
    }

    private void initGL() {
        // 2D Initialization
        glClearColor(0, 0, 0, 0);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_ALPHA_TEST);
        glEnable(GL_BLEND);
        glAlphaFunc(GL_GREATER, 0);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); // Wireframe
    }

    private void resizeGL(int width, int height) {
        // 2D Scene
        glViewport(0, 0, width, height);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluOrtho2D(0, width, height, 0);
        glPushMatrix();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glPushMatrix();
    }
    
    private void processKeyboard() {
        while (Keyboard.next()) {
            for (GUI gui : guiList)
                if (!gui.processKeyboardEvent()) break;
        }
    }

    private void processMouse() {
        while (Mouse.next()) {
            for (GUI gui : guiList)
                if (!gui.processMouseEvent()) break;
        }
    }
    
    private void update() {
        Iterator it = guiList.iterator();
        while (it.hasNext()) {
            GUI gui = (GUI)it.next();
            if (gui.isRemoveRequested()) it.remove();
            else gui.update();
        }
    }
    
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        glLoadIdentity();
        
        //texture.getTexture("pegs/large/red.png").bind();
        //RenderHelper.drawSquare(0, 0, 32, 32, true);
        
        for (GUI gui : guiList)
            gui.render();
    }
    
    private void run() throws LWJGLException {
        //Dimension newDim;
        while(!Display.isCloseRequested() && !closeRequested) {
            //newDim = newCanvasSize.getAndSet(null);
            //if (newDim != null) resizeGL(newDim.width, newDim.height);
            
            fps = Math.round(1000000000D / Math.max((double)System.nanoTime() - (double)fpsLastFrame, 1D));
            fpsLastFrame = System.nanoTime(); 
            if(Display.isVisible()) {
                mouseHitbox.setLocation(getMouseX(), getMouseY());
                this.processKeyboard();
                this.processMouse();
                this.update();
                this.render();
                tick.incTicks();
            }
            else {
                if(Display.isDirty()) {
                    this.render();
                }
                try {
                    Thread.sleep(100);
                }
                catch(InterruptedException ex) {
                }
            }
            Display.update();
            Display.sync(Constants.FRAME_RATE);
        }
    }

    public FontManager getFontManager() {
        return font;
    }

    public TextureManager getTextureManager() {
        return texture;
    }

    public SoundManager getSoundManager() {
        return sound;
    }

    /*public Vector2f getViewportScale() {
        return new Vector2f((float)canvas.getWidth() / (float)displayMode.getWidth(), (float)canvas.getHeight() / (float)displayMode.getHeight());
    }*/
    
    public int getMouseX() {
        //return (int)(Mouse.getX() / ((double)canvas.getWidth() / (double)displayMode.getWidth()));
        return (int)(Mouse.getX());
    }
    
    public int getMouseY() {
        //return (int)((canvas.getHeight() - Mouse.getY()) / ((double)canvas.getHeight() / (double)displayMode.getHeight()));
        return (int)(canvas.getHeight() - Mouse.getY());
    }
    
    public Vector2f getMousePos() {
        return new Vector2f(getMouseX(), getMouseY());
    }
    
    public void setMouseX(int x) {
        setMousePos(x, getMouseY());
    }
    
    public void setMouseY(int y) {
        setMousePos(getMouseX(), y);
    }
    
    public void setMousePos(int x, int y) {
        int new_x = (int)(x * ((double)canvas.getWidth() / (double)displayMode.getWidth()));
        int new_y = (int)((displayMode.getHeight() - y) * ((double)canvas.getHeight() / (double)displayMode.getHeight()));
        Mouse.setCursorPosition(new_x, new_y);
    }
    
    public void setMousePos(Vector2f pos) {
        setMousePos((int)pos.x, (int)pos.y);
    }

    public Shape getMouseHitbox() {
        return mouseHitbox;
    }

    public int getWidth() {
        return displayMode.getWidth();
    }

    public int getHeight() {
        return displayMode.getHeight();
    }

    public int getFrequency() {
        return displayMode.getFrequency();
    }

    public int getBitsPerPixel() {
        return displayMode.getBitsPerPixel();
    }
    
    public List<GUI> getGUIList() {
        return Collections.unmodifiableList(guiList);
    }

    public TickCounter getTick() {
        return tick;
    }

    public long getFps() {
        return fps;
    }

    public Frame getFrame() {
        return frame;
    }

    public PixelFormat getPixelFormat() {
        return pixelFormat;
    }

    public void setPixelFormat(PixelFormat pixelFormat) {
        this.pixelFormat = pixelFormat;
    }
    
    public void shutdown() {
        closeRequested = true;
    }
    
    private void shutdownInternal() {
        config.save();
        sound.getSoundSystem().cleanup();
        texture.cleanup();
        font.cleanup();
        Display.destroy();
        Keyboard.destroy();
        Mouse.destroy();
    }
    
    public File getDataDirectory() {
        return dataDir;
    }
}