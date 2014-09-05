package com.techjar.jfos2.client;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.util.glu.GLU.*;

import com.techjar.jfos2.ConfigManager;
import com.techjar.jfos2.Constants;
import org.lwjgl.opengl.DisplayMode;
import com.techjar.jfos2.OperatingSystem;
import com.techjar.jfos2.TickCounter;
import com.techjar.jfos2.util.Util;
import com.techjar.jfos2.client.gui.*;
import com.techjar.jfos2.client.gui.screen.Screen;
import com.techjar.jfos2.entity.EntityShip;
import com.techjar.jfos2.util.AsteroidGenerator;
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
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
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
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.ShapeRenderer;
import org.newdawn.slick.geom.TexCoordGenerator;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.renderer.ImmediateModeOGLRenderer;

/**
 *
 * @author Techjar
 */
public class Client {
    private static Client instance;
    private final File dataDir;
    private Frame frame;
    private Canvas canvas;
    private Rectangle mouseHitbox;
    private ConfigManager config;
    private FontManager fontManager;
    private TextureManager textureManager;
    private SoundManager soundManager;
    private DisplayMode displayMode;
    private boolean borderless;
    private boolean wasBorderless;
    private List<DisplayMode> displayModeList;
    private PixelFormat pixelFormat;
    private TickCounter tick;
    private List<Screen> screenList;
    private List<GUICallback> resizeHandlers;
    private List<Runnable> preProcessors;
    private List<Runnable> postProcessors;
    //private AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
    private int fps;
    private int fpsRender;
    private long timeLastFrame;
    private long timeSpentLastFrame;
    private boolean closeRequested;
    private boolean arbSupported;
    private boolean running = true;
    private int arbMaxSamples;
    private boolean renderBackground;
    private List<String> validControllers = new ArrayList<>();
    private List<Shape> testShapes = new ArrayList<>();
    
    private int tex;
    private int fbo;

    // Some State Junk
    private boolean resourcesDone;
    private boolean offline;
    private boolean gameStarted;
    private String titleMusic;
    private boolean titleStarted;
    private boolean titleScreenVisible;
    private TickCounter titleTick;
    private UnicodeFont introFont;
    
    
    public Client() {
        dataDir = OperatingSystem.getDataDirectory("jfos2");
        mouseHitbox = new Rectangle(0, 0, 1, 1);
        tick = new TickCounter(Constants.TICK_RATE);
        screenList = new ArrayList<>();
        resizeHandlers = new ArrayList<>();
        preProcessors = new ArrayList<>();
        postProcessors = new ArrayList<>();
    }
    
    public static void main(String[] args) {
        if (getInstance() != null) throw new IllegalStateException("Client already running!");
        try {
            instance = new Client();
            for (String arg : args) {
                switch (arg) {
                    case "--offline":
                        instance.offline = true;
                        break;
                    default:
                        System.out.println("Unknown argument: " + arg);
                        break;
                }
            }
            getInstance().start();
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

    public static Client getInstance() {
        return instance;
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
        Display.setVSyncEnabled(true);

        textureManager = new TextureManager();
        Display.create();
        init();
        drawSplash();
        Display.update();

        Controllers.create();
        for (int i = 0; i < Controllers.getControllerCount(); i++) {
            Controller con = Controllers.getController(i);
            if (con.getAxisCount() >= 2) {
                validControllers.add(con.getName());
                config.defaultProperty("controls.controller", con.getName());
                System.out.println("Found controller: " + con.getName() + " (" + con.getRumblerCount() + " Rumblers)");
            }
        }
        if (validControllers.size() < 1) config.setProperty("controls.controller", "");
        else if (!validControllers.contains(config.getString("controls.controller"))) config.setProperty("controls.controller", validControllers.get(0));
        config.save();
        
        Keyboard.create();
        Mouse.create();
        Mouse.setGrabbed(false);
        fontManager = new FontManager();
        soundManager = new SoundManager();
        soundManager.setEffectVolume(config.getFloat("sound.effectvolume"));
        soundManager.setMusicVolume(config.getFloat("sound.musicvolume"));

        if (offline) {
            resourcesDone = true;
            preloadData();
        }
        else {
            fontManager.getFont("batmfa_", 50, false, false);
            ResourceDownloader.checkAndDownload();
        }
        introFont = fontManager.getFont("astronbo", 64, true, false).getUnicodeFont();
        
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
        GUITabbed tab = new GUITabbed(fontManager.getFont("COPRGTB", 16, false, false).getUnicodeFont(), new Color(Color.WHITE), new GUIBackground(new Color(200, 0, 0), new Color(50, 50, 50), 1));
        tab.setDimension(500, 500);
        tab.setPosition(100, 50);
        //guiList.add(tab);
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
        GUITextField guit = new GUITextField(fontManager.getFont("COPRGTB", 24, false, false).getUnicodeFont(), new Color(Color.WHITE), new GUIBackground(new Color(0, 0, 0), new Color(200, 0, 0), 2));
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
        //sound.playMusic("music/title.mp3", true);
        
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
        if (getInstance() != null) {
            try {
                if (getInstance().frame != null) {
                    getInstance().frame.setResizable(false);
                    getInstance().frame.setSize(new Dimension(getInstance().displayMode.getWidth(), getInstance().displayMode.getHeight()));
                    // todo
                    while (!getInstance().closeRequested) {
                        try { Thread.sleep(100); }
                        catch (InterruptedException junk) { }
                    }
                }
                getInstance().shutdownInternal();
            } catch (Throwable ex2) {
                ex2.printStackTrace();
                System.exit(0);
            }
        }
    }

    private void initDisplayModes() throws LWJGLException {
        displayModeList = new ArrayList<>();
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

        config.setProperty("version", Constants.VERSION);
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
        List<String> list = new ArrayList<>(displayModeList.size());
        for (DisplayMode mode : displayModeList) {
            list.add(new StringBuilder().append(mode.getWidth()).append("x").append(mode.getHeight()).toString());
        }
        return Collections.unmodifiableList(list);
    }

    public int addResizeHandler(GUICallback resizeHandler) {
        if (resizeHandlers.add(resizeHandler))
            return resizeHandlers.indexOf(resizeHandler);
        return -1;
    }

    public void removeResizeHandler(GUICallback resizeHandler) {
        resizeHandlers.remove(resizeHandler);
    }

    public void removeResizeHandler(int index) {
        resizeHandlers.remove(index);
    }

    public void clearResizeHandlers() {
        resizeHandlers.clear();
    }

    public void addPreProcesor(Runnable processor) {
        preProcessors.add(processor);
    }

    public void removePreProcesor(Runnable processor) {
        preProcessors.remove(processor);
    }

    public void addPostProcesor(Runnable processor) {
        postProcessors.add(processor);
    }

    public void removePostProcesor(Runnable processor) {
        postProcessors.remove(processor);
    }

    private void preloadData() {
        // Preload Textures
        textureManager.getTexture("ui/windowclose.png");
        
        // Preload Sounds
        soundManager.loadSound("ui/click.wav");
        soundManager.loadSound("ui/rollover.wav");
    }
    
    private void drawBackground() {
        textureManager.getTexture("background.png").bind();
        RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), true);
    }

    private void drawSplash() {
        drawBackground();
    }

    private void gameStartupStuff() {
        if (!resourcesDone) {
            if (ResourceDownloader.isCompleted()) {
                resourcesDone = true;
                preloadData();
                initIntro();
            }
            else {
                drawBackground();
                UnicodeFont theFont = fontManager.getFont("batmfa_", 50, false, false).getUnicodeFont();
                int width = theFont.getWidth(ResourceDownloader.getStatus());
                Vector2f fontCenter = getScreenCenter(width, 0);
                theFont.drawString(fontCenter.getX(), fontCenter.getY() - 60, ResourceDownloader.getStatus(), org.newdawn.slick.Color.white);
                float barWidth = 600;
                Vector2f barCenter = getScreenCenter((int)barWidth, 0);
                RenderHelper.drawSquare(barCenter.getX(), barCenter.getY(), barWidth, 30, new Color(50, 50, 50));
                RenderHelper.drawSquare(barCenter.getX(), barCenter.getY(), barWidth * ResourceDownloader.getProgress(), 30, new Color(0, 100, 0));
            }
        }
        if (titleStarted || soundManager.isPlaying(titleMusic)) {
            titleStarted = true;
            if (titleTick.getTickMillis() > 12000) gameStarted = true;
            if (titleTick.getTickMillis() > 11000 && !titleScreenVisible) {
                titleScreenVisible = true;
                GUICreator.setupTitleScreen(this);
            }
            if (titleTick.getTickMillis() < 5400) {
                String str = "Techjar Presents";
                int width = introFont.getWidth(str), height = introFont.getHeight(str);
                Vector2f fontCenter = getScreenCenter(width, height);
                introFont.drawString(fontCenter.getX(), fontCenter.getY(), str, org.newdawn.slick.Color.white);
            }
            if (titleTick.getTickMillis() > 6400 && titleTick.getTickMillis() < 10500) {
                String str = "Yet another space shooter...";
                int width = introFont.getWidth(str), height = introFont.getHeight(str);
                Vector2f fontCenter = getScreenCenter(width, height);
                introFont.drawString(fontCenter.getX(), fontCenter.getY(), str, org.newdawn.slick.Color.white);
            }
            if (titleTick.getTickMillis() > 11000 && titleTick.getTickMillis() < 12000) {
                RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * (1 - ((float)(titleTick.getTickMillis() - 11000) / 1000f)))));
            }
            if (titleTick.getTickMillis() > 9500 && titleTick.getTickMillis() < 10500) {
                RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * ((float)(titleTick.getTickMillis() - 9500) / 1000f))));
            }
            if (titleTick.getTickMillis() > 6400 && titleTick.getTickMillis() < 7400) {
                RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * (1 - ((float)(titleTick.getTickMillis() - 6400) / 1000f)))));
            }
            if (titleTick.getTickMillis() > 4400 && titleTick.getTickMillis() < 5400) {
                RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * ((float)(titleTick.getTickMillis() - 4400) / 1000f))));
            }
            if (titleTick.getTickMillis() < 1000) {
                RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), new Color(0, 0, 0, (int)(255 * (1 - ((float)titleTick.getTickMillis() / 1000f)))));
            }
            titleTick.incTicks();
        }
    }

    private void initIntro() {
        titleMusic = soundManager.playMusic("music/title.mp3", true);
        titleTick = new TickCounter(Constants.TICK_RATE);
    }
    
    private void init() {
        initGL();
        resizeGL(displayMode.getWidth(), displayMode.getHeight());

        fbo = glGenFramebuffersEXT();
        tex = glGenTextures();

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fbo);

        glBindTexture(GL_TEXTURE_2D, tex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 512, 512, 0, GL_RGBA, GL_INT, (ByteBuffer)null);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, tex, 0);

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    private void initGL() {
        // 2D Initialization
        glClearColor(0, 0, 0, 0);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_ALPHA_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_MULTISAMPLE);
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
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    private void preProcess() {
        for (Runnable runnable : preProcessors) {
            runnable.run();
        }
    }
    
    private void processKeyboard() {
        toploop: while (Keyboard.next()) {
            for (Screen screen : screenList)
                if (!screen.processKeyboardEvent()) continue toploop;
        }
    }

    private void processMouse() {
        toploop: while (Mouse.next()) {
            for (Screen screen : screenList)
                if (!screen.processMouseEvent()) continue toploop;
        }
    }

    private void processController() {
        toploop: while (Controllers.next()) {
            Controller con = Controllers.getEventSource();
            if (con.getName().equals(config.getString("controls.controller"))) {
                for (Screen screen : screenList)
                    if (!screen.processControllerEvent(con)) continue toploop;
            }
        }
    }

    private void update() {
        for (Screen screen : screenList)
            screen.update();
        //throw new RuntimeException();
    }
    
    private void render() {
        long time = System.nanoTime();
        glClear(GL_COLOR_BUFFER_BIT);
        glLoadIdentity();
        
        if (renderBackground) drawBackground();

        for (Screen screen : screenList)
            screen.render();
        if (!gameStarted) gameStartupStuff(); // We're gonna be a bit dirty here and do "update" code in the render method, makes things easier.
        Runtime runtime = Runtime.getRuntime();
        fontManager.getFont("batmfa_", 20, false, false).getUnicodeFont().drawString(5, 5, Long.toString(fpsRender), org.newdawn.slick.Color.yellow);
        fontManager.getFont("batmfa_", 20, false, false).getUnicodeFont().drawString(5, 25, "Memory: " + Util.bytesToMBString(runtime.totalMemory() - runtime.freeMemory()) + " / " + Util.bytesToMBString(runtime.maxMemory()), org.newdawn.slick.Color.yellow);
        /*long time = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            Shape shape = new Polygon(AsteroidGenerator.generate(50, 10, 10, 30));
        }
        System.out.println(System.nanoTime() - time);*/
        if (tick.getTickMillis() % 500 == 0) {
        //if (testShapes.isEmpty()) {
            testShapes.clear();
            Shape shape = new Polygon(AsteroidGenerator.generatePoints(130, 10, 5, 10, 30));
            //shape.setCenterX(displayMode.getWidth() / 2);
            //shape.setCenterY(displayMode.getHeight() / 2);
            shape.setCenterX(256);
            shape.setCenterY(256);
            Shape shape2 = shape.transform(Transform.createScaleTransform((shape.getWidth() + 6) / shape.getWidth(), (shape.getHeight() + 6) / shape.getHeight()));
            //shape2.setCenterX(displayMode.getWidth() / 2);
            //shape2.setCenterY(displayMode.getHeight() / 2);
            shape2.setCenterX(256);
            shape2.setCenterY(256);
            testShapes.add(shape2);
            testShapes.add(shape);
            testShapes.addAll(Arrays.asList(AsteroidGenerator.generateCraters(shape, 10, 5, 200)));
            //System.out.println(shape2.getMinX() + " " + shape2.getMinY() + " " + shape2.getMaxX() + " " + shape2.getMaxY());

            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fbo);
            resizeGL(512, 512);
            glBindTexture(GL_TEXTURE_2D, 0);
            
            //glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            glClear(GL_COLOR_BUFFER_BIT);
            glLoadIdentity();
            
            for (int i = 0; i < testShapes.size(); i++) {
                Shape sh = testShapes.get(i);
                if (i == 0) glColor3f(1, 0, 0);
                if (i == 1) glColor3f(1, 1, 0);
                if (i == 2) glColor3f(0, 1, 1);
                //glColor3f(1, 1, 1);
                //ShapeRenderer.fill(sh);
                glEnable(GL_TEXTURE_2D);
                final Texture texture = textureManager.getTexture("asteroid.png", GL_NEAREST);
                texture.bind();
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT); // Temp code for Reference
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT); // Temp code for Reference
                /*glBegin(GL_QUADS);
                glTexCoord2f(0, 0); glVertex2f(0, 0);
                glTexCoord2f(8, 0); glVertex2f(512, 0);
                glTexCoord2f(8, 8); glVertex2f(512, 512);
                glTexCoord2f(0, 8); glVertex2f(0, 512);
                glEnd();*/
                /*try {
                    Field f = ShapeRenderer.class.getDeclaredField("GL");
                    f.setAccessible(true);
                Object obj = f.get(null);
                if (obj instanceof ImmediateModeOGLRenderer) System.out.println("YAY");
                else System.out.println("BOO");
                }catch(Exception ex){ex.printStackTrace();}*/
                //ShapeRenderer.texture(sh, textureManager.getImage("asteroid.png"));
                /*ShapeRenderer.texture(sh, new Image(textureManager.getTexture("asteroid.png")), new TexCoordGenerator() {
                    @Override
                    public org.newdawn.slick.geom.Vector2f getCoordFor(float x, float y) {
                        return new org.newdawn.slick.geom.Vector2f(x / (float)texture.getImageWidth(), y /  (float)texture.getImageHeight());
                    }
                });*/
                ShapeRenderer.textureFit(sh, textureManager.getImage("asteroid.png"), sh.getWidth() / texture.getTextureWidth(), sh.getHeight() / texture.getTextureHeight());
            }
            //glBindTexture(GL_TEXTURE_2D, tex);
            //ByteBuffer pixels = ByteBuffer.allocateDirect(4 * 4 * 4);
            //glReadPixels(0, 0, (int)Math.ceil(shape2.getWidth()), (int)Math.ceil(shape2.getHeight()), GL_BGRA, GL_UNSIGNED_BYTE, pixels);
            //glBlitFramebuffer(0, 0, 512, 512, 0, 0, 512, 512, GL_COLOR_BUFFER_BIT, GL_NEAREST);
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
            resizeGL(displayMode.getWidth(), displayMode.getHeight());
        }
        if (tex > 0) {
            Shape shape = testShapes.get(0);
            glBindTexture(GL_TEXTURE_2D, tex);
            //Circle c = new Circle(0, 0, shape.getBoundingCircleRadius());
            //System.out.println(shape.getMaxY() - shape.getMinY());
            RenderHelper.drawSquare(100, 100, 512, 512, (Color)Color.WHITE, true);
        }
        //System.out.println(System.nanoTime() - time);
    }

    private void postProcess() {
        for (Runnable runnable : postProcessors) {
            runnable.run();
        }
    }
    
    private void run() throws LWJGLException {
        //Dimension newDim;
        while(!Display.isCloseRequested() && !closeRequested) {
            //newDim = newCanvasSize.getAndSet(null);
            //if (newDim != null) resizeGL(newDim.width, newDim.height);

            timeSpentLastFrame = System.nanoTime() - timeLastFrame;
            fps = (int)Math.round(1000000000D / (double)timeSpentLastFrame);
            timeLastFrame = System.nanoTime();
            if (tick.getTicks() % 6 == 0) fpsRender = fps;
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
            Display.sync(Constants.TICK_RATE);
        }
    }

    public FontManager getFontManager() {
        return fontManager;
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public ConfigManager getConfigManager() {
        return config;
    }

    public Controller getController(String name) {
        for (int i = 0; i < Controllers.getControllerCount(); i++) {
            Controller con = Controllers.getController(i);
            if (con.getName().equals(name)) return con;
        }
        return null;
    }

    /*public Vector2f getViewportScale() {
        return new Vector2f((float)canvas.getWidth() / (float)displayMode.getWidth(), (float)canvas.getHeight() / (float)displayMode.getHeight());
    }*/

    public Vector2f getScreenCenter(org.lwjgl.util.Dimension dim) {
        return new Vector2f((displayMode.getWidth() / 2f) - (dim.getWidth() / 2f), (displayMode.getHeight() / 2f) - (dim.getHeight() / 2f));
    }

    public Vector2f getScreenCenter(int width, int height) {
        return getScreenCenter(new org.lwjgl.util.Dimension(width, height));
    }

    public Vector2f getScreenCenter() {
        return getScreenCenter(new org.lwjgl.util.Dimension());
    }

    public Controller getActiveController() {
        for (int i = 0; i < Controllers.getControllerCount(); i++) {
            Controller con = Controllers.getController(i);
            if (con.getName().equals(config.getString("controls.controller"))) {
                return con;
            }
        }
        return null;
    }
    
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
    
    public List<Screen> getScreenList() {
        return Collections.unmodifiableList(screenList);
    }

    public void addScreen(Screen screen) {
        screenList.add(screen);
    }

    public void removeScreen(Screen screen) {
        screenList.remove(screen);
    }

    public void removeScreen(int index) {
        screenList.remove(index);
    }

    public void clearScreens() {
        screenList.clear();
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

    public boolean isRunning() {
        return running;
    }

    public boolean isRenderBackground() {
        return renderBackground;
    }

    public void setRenderBackground(boolean renderBackground) {
        this.renderBackground = renderBackground;
    }
    
    public void shutdown() {
        closeRequested = true;
    }
    
    private void shutdownInternal() {
        running = false;
        config.save();
        soundManager.getSoundSystem().cleanup();
        textureManager.cleanup();
        fontManager.cleanup();
        Display.destroy();
        Keyboard.destroy();
        Mouse.destroy();
    }
    
    public File getDataDirectory() {
        return dataDir;
    }
}
