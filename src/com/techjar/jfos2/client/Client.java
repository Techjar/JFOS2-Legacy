package com.techjar.jfos2.client;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicReference;
import net.java.games.input.ControllerEnvironment;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.ShapeRenderer;

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
    private boolean borderless;
    private boolean wasBorderless;
    private List<DisplayMode> displayModeList;
    private PixelFormat pixelFormat;
    private TickCounter tick;
    private List<GUI> guiList;
    private List<GUICallback> resizeHandlers;
    //private AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
    private long fps;
    private long fpsLastFrame;
    private boolean closeRequested;
    private boolean arbSupported;
    private int arbMaxSamples;
    
    
    public Client() {
        dataDir = OperatingSystem.getDataDirectory("jfos2");
        mouseHitbox = new Rectangle(0, 0, 1, 1);
        tick = new TickCounter(Constants.FRAME_RATE);
        guiList = new ArrayList<GUI>();
        resizeHandlers = new ArrayList<GUICallback>();
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
    
    public void start() throws LWJGLException, SlickException, IOException {
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        initDisplayModes();
        initConfig();
        
        PixelFormat format = new PixelFormat(displayMode.getBitsPerPixel(), 0, 24, 8, 0);
        Pbuffer pb = new Pbuffer(640, 480, format, null);
        pb.makeCurrent();
        arbSupported = GLContext.getCapabilities().GL_ARB_multisample;
        if(arbSupported) arbMaxSamples = glGetInteger(GL_MAX_SAMPLES);
        pb.destroy();
        
        makeFrame(borderless);
        Display.setDisplayMode(displayMode);

        texture = new TextureManager();
        Display.create();
        init();
        drawSplash();
        Display.update();
        
        Keyboard.create();
        Controllers.create();
        Mouse.create();
        Mouse.setGrabbed(false);
        font = new FontManager();
        sound = new SoundManager();
        sound.setEffectVolume(config.getFloat("sound.effectvolume"));
        sound.setMusicVolume(config.getFloat("sound.musicvolume"));
        preloadData();
        
        GUIWindow slider = new GUIWindow(new GUIBackground());
        slider.setDimension(500, 500);
        slider.setPosition(56, 50);
        GUIWindow win1 = new GUIWindow(new GUIBackground());
        win1.setDimension(500, 500);
        win1.setPosition(56, 50);
        GUIWindow win2 = new GUIWindow(new GUIBackground());
        win2.setDimension(500, 500);
        win2.setPosition(56, 50);
        GUIWindow win3 = new GUIWindow(new GUIBackground());
        win3.setDimension(500, 500);
        win3.setPosition(56, 50);
        /*slider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                GUISlider slid = (GUISlider)this.getComponent();
                sound.setMasterVolume(slid.getValue());
            }
        });*/
        //guiList.add(slider);
        //guiList.add(win1);
        //guiList.add(win2);
        //guiList.add(win3);
        GUITabbed tab = new GUITabbed(font.getFont("COPRGTB", 16, false, false).getUnicodeFont(), new Color(Color.WHITE), new GUIBackground(new Color(200, 0, 0), new Color(50, 50, 50), 1));
        tab.setDimension(500, 500);
        tab.setPosition(100, 50);
        guiList.add(tab);
        GUIContainer thing = new GUIScrollBox(new Color(100, 0, 0));
        thing.setDimension((int)slider.getContainerBox().getWidth(), (int)slider.getContainerBox().getHeight());
        GUIContainer thingo = new GUIScrollBox(new Color(100, 0, 0));
        thingo.setDimension((int)slider.getContainerBox().getWidth(), (int)slider.getContainerBox().getHeight());
        //slider.addComponent(thing);
        tab.addTab("Poop Butt", thing);
        tab.addTab("Piss Balls", thingo);
        GUIRadioButton b1 = new GUIRadioButton(new Color(Color.WHITE), new GUIBackground(new Color(0, 0, 0), new Color(200, 0, 0), 2));
        b1.setDimension(30, 30);
        b1.setPosition(40, 80);
        GUIRadioButton b2 = new GUIRadioButton(new Color(Color.WHITE), new GUIBackground(new Color(0, 0, 0), new Color(200, 0, 0), 2));
        b2.setDimension(30, 30);
        b2.setPosition(40, 120);
        GUIRadioButton b3 = new GUIRadioButton(new Color(Color.WHITE), new GUIBackground(new Color(0, 0, 0), new Color(200, 0, 0), 2));
        b3.setDimension(30, 30);
        b3.setPosition(40, 160);
        GUIRadioButton b4 = new GUIRadioButton(new Color(Color.WHITE), new GUIBackground(new Color(0, 0, 0), new Color(200, 0, 0), 2));
        b4.setDimension(30, 30);
        b4.setPosition(40, 200);
        thing.addComponent(b1);
        thing.addComponent(b2);
        thing.addComponent(b3);
        thing.addComponent(b4);
        GUITextField guit = new GUITextField(font.getFont("COPRGTB", 24, false, false).getUnicodeFont(), new Color(Color.WHITE), new GUIBackground(new Color(0, 0, 0), new Color(200, 0, 0), 2));
        guit.setDimension(200, 30);
        guit.setPosition(40, 240);
        thingo.addComponent(guit);
        //GUIInputOption thing2 = new GUIInputOption(font.getFont("COPRGTB", 24, false, false).getUnicodeFont(), new Color(200, 0, 0));
        //GUIComboBox thing2 = new GUIComboBox(font.getFont("Nighb___", 24, false, false).getUnicodeFont(), new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(200, 0, 0), 2));
        //GUIComboButton thing2 = new GUIComboButton(font.getFont("Nighb___", 24, false, false).getUnicodeFont(), new Color(200, 0, 0));
        GUICheckBox thing2 = new GUICheckBox(new Color(Color.WHITE), new GUIBackground(new Color(0, 0, 0), new Color(200, 0, 0), 2));
        thing2.setDimension(30, 30);
        thing2.setPosition(40, 40);
        /*thing2.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                Client.client.setBorderless(!Client.client.isBorderless());
                Client.client.useDisplayMode();
            }
        });*/
        //thing2.addAllItems(this.getDisplayModeStrings());
        thing.addComponent(thing2);
        sound.playMusic("music/title.mp3", true);
        
        run();
        shutdownInternal();
        System.exit(0);
    }

    private void makeFrame(boolean undecorated) throws LWJGLException {
        frame = new Frame(Constants.GAME_TITLE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        frame.setAlwaysOnTop(false);
        frame.setUndecorated(undecorated);
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

        canvas.setPreferredSize(new Dimension(displayMode.getWidth(), displayMode.getHeight()));
        frame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((dim.width - frame.getSize().width) / 2, (dim.height - frame.getSize().height) / 2);
        frame.setVisible(true);
        Display.setParent(canvas);
    }

    public static void crashException(Throwable ex) {
        ex.printStackTrace();
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
        config.defaultProperty("display.borderless", false);
        config.defaultProperty("sound.effectvolume", 1f);
        config.defaultProperty("sound.musicvolume", 1f);

        if (!setDisplayMode(config.getInteger("display.width"), config.getInteger("display.height"))) {
            config.setProperty("display.width", displayMode.getWidth());
            config.setProperty("display.height", displayMode.getHeight());
        }
        borderless = config.getBoolean("display.borderless");
        wasBorderless = borderless;

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

    public void useDisplayMode() {
        try {
            actuallyUseDisplayMode(displayMode, borderless);
            resizeGL(displayMode.getWidth(), displayMode.getHeight());
            for (GUICallback callback : resizeHandlers) {
                callback.run();
            }
        }
        catch (LWJGLException ex) {
            crashException(ex);
            System.exit(0);
        }
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public boolean setDisplayMode(int width, int height) {
        DisplayMode mode = findDisplayMode(width, height);
        if (mode != null) {
            setDisplayMode(mode);
            return true;
        }
        return false;
    }

    public boolean isBorderless() {
        return borderless;
    }

    public void setBorderless(boolean borderless) {
        this.borderless = borderless;
    }

    private void actuallyUseDisplayMode(DisplayMode displayMode, boolean borderless) throws LWJGLException {
        Display.setDisplayMode(displayMode);
        if (borderless != wasBorderless) {
            Frame oldFrame = frame;
            oldFrame.setVisible(false);
            makeFrame(borderless);
            oldFrame.dispose();
            wasBorderless = borderless;
        }
    }

    public List<DisplayMode> getDisplayModeList() {
        return Collections.unmodifiableList(displayModeList);
    }

    public List<String> getDisplayModeStrings() {
        List<String> list = new ArrayList<String>(displayModeList.size());
        for (DisplayMode mode : displayModeList) {
            list.add(new StringBuilder().append(mode.getWidth()).append("x").append(mode.getHeight()).toString());
        }
        return Collections.unmodifiableList(list);
    }

    public void addResizeHandler(GUICallback resizeHandler) {
        resizeHandlers.add(resizeHandler);
    }

    public void removeResizeHandler(GUICallback resizeHandler) {
        resizeHandlers.remove(resizeHandler);
    }

    private void preloadData() {
        // Preload Textures
        texture.getTexture("ui/windowclose.png");
        
        // Preload Sounds
        sound.loadSound("ui/click.wav");
        sound.loadSound("ui/rollover.wav");
    }
    
    private void drawSplash() {
        //Shape shape = new Rectangle(0, 0, displayMode.getWidth(), displayMode.getHeight());
        //Shape shape = new Rectangle(0, 0, 128, 128);
        Shape shape = new Rectangle(0, 0, displayMode.getWidth(), displayMode.getHeight());
        ShapeRenderer.textureFit(shape, new Image(texture.getTexture("background.png")));
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
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); // Wireframe
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT); // Temp code for Reference
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT); // Temp code for Reference
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

    private void preProcess() {
    }
    
    private void processKeyboard() {
        while (Keyboard.next()) {
            for (GUI gui : guiList)
                if (gui.isVisible() && gui.isEnabled() && !gui.processKeyboardEvent()) break;
        }
    }

    private void processMouse() {
        while (Mouse.next()) {
            for (GUI gui : guiList)
                if (gui.isVisible() && gui.isEnabled() && !gui.processMouseEvent()) break;
        }
    }

    private void processController() {
        
    }

    private void update() {
        GUIWindow lastWin = null, lastTopWin = null;
        List<GUI> toAdd = new ArrayList<GUI>();
        Iterator it = guiList.iterator();
        while (it.hasNext()) {
            GUI gui = (GUI)it.next();
            if (gui.isRemoveRequested()) it.remove();
            else {
                if (gui.isVisible() && gui.isEnabled()) {
                    gui.update();
                    if (gui.isRemoveRequested()) it.remove();
                    else if (gui instanceof GUIWindow) {
                        GUIWindow win = (GUIWindow)gui;
                        if (lastWin != null && lastWin != lastTopWin) lastWin.setOnTop(false);
                        lastWin = win;
                        win.setOnTop(true);
                        if (win.isToBePutOnTop()) {
                            it.remove();
                            toAdd.add(gui);
                            win.setToBePutOnTop(false);
                            if (lastTopWin != null) lastTopWin.setOnTop(false);
                            lastTopWin = win;
                        }
                    }
                }
            }
        }
        guiList.addAll(toAdd);
    }
    
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        glLoadIdentity();
        
        //texture.getTexture("pegs/large/red.png").bind();
        //RenderHelper.drawSquare(0, 0, 32, 32, true);

        for (GUI gui : guiList)
            if (gui.isVisible()) gui.render();
    }

    private void postProcess() {
    }
    
    private void run() throws LWJGLException {
        //Dimension newDim;
        while(!Display.isCloseRequested() && !closeRequested) {
            //newDim = newCanvasSize.getAndSet(null);
            //if (newDim != null) resizeGL(newDim.width, newDim.height);
            
            fps = Math.round((double)Sys.getTimerResolution() / (double)(Sys.getTime() - fpsLastFrame));
            fpsLastFrame = Sys.getTime();
            if(Display.isVisible()) {
                mouseHitbox.setLocation(getMouseX(), getMouseY());
                this.preProcess();
                this.processKeyboard();
                this.processMouse();
                this.processController();
                this.update();
                this.render();
                this.postProcess();
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
        return (int)(displayMode.getHeight() - Mouse.getY());
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
