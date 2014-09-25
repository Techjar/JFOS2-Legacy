package com.techjar.jfos2.client;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.util.glu.GLU.*;

import com.techjar.jfos2.util.ConfigManager;
import com.techjar.jfos2.util.Constants;
import org.lwjgl.opengl.DisplayMode;
import com.techjar.jfos2.util.OperatingSystem;
import com.techjar.jfos2.util.Util;
import com.techjar.jfos2.client.gui.*;
import com.techjar.jfos2.client.gui.screen.Screen;
import com.techjar.jfos2.client.gui.screen.ScreenIntro;
import com.techjar.jfos2.world.ClientWorld;
import com.techjar.jfos2.util.ArgumentParser;
import com.techjar.jfos2.util.Vector2;
import com.techjar.jfos2.util.logging.LogHelper;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JFrame;
import lombok.Value;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public class Client {
    private static Client instance;
    private final File dataDir;
    private JFrame frame;
    private Canvas canvas;
    private Rectangle mouseHitbox;
    private ConfigManager config;
    private FontManager fontManager;
    private TextureManager textureManager;
    private SoundManager soundManager;
    private DisplayMode displayMode;
    private DisplayMode newDisplayMode;
    private DisplayMode configDisplayMode;
    private boolean fullscreen;
    private boolean newFullscreen;
    private List<DisplayMode> displayModeList;
    private PixelFormat pixelFormat;
    private List<Screen> screenList;
    private List<ScreenHolder> screensToAdd;
    private List<GUICallback> resizeHandlers;
    private List<Runnable> preProcessors;
    private List<Runnable> postProcessors;
    private int multisampleFBO;
    private int multisampleTexture;
    //private AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
    private int fpsCounter;
    private int fpsRender;
    private long timeCounter;
    private long deltaTime;
    private boolean closeRequested;
    private boolean hasCrashed;
    public final boolean antiAliasingSupported;
    public final int antiAliasingMaxSamples;
    private boolean antiAliasing;
    private int antiAliasingSamples = 4;
    private boolean running;
    private boolean renderBackground;
    public boolean renderDebug = true;
    private Map<String, Integer> validControllers = new HashMap<>();
    private ClientWorld world;

    // Some State Junk
    private boolean resourcesDone;
    private boolean offline;
    
    
    public Client() throws LWJGLException {
        System.setProperty("sun.java2d.noddraw", "true");
        LogHelper.init();
        dataDir = OperatingSystem.getDataDirectory("jfos2");
        mouseHitbox = new Rectangle(0, 0, 1, 1);
        screenList = new ArrayList<>();
        screensToAdd = new ArrayList<>();
        resizeHandlers = new ArrayList<>();
        preProcessors = new ArrayList<>();
        postProcessors = new ArrayList<>();
        
        PixelFormat format = new PixelFormat(32, 0, 24, 8, 0);
        Pbuffer pb = new Pbuffer(800, 600, format, null);
        pb.makeCurrent();
        antiAliasingMaxSamples = glGetInteger(GL_MAX_SAMPLES);
        antiAliasingSupported = antiAliasingMaxSamples > 0;
        pb.destroy();
        LogHelper.config("AA Supported: %s / Max Samples: %d", antiAliasingSupported ? "yes" : "no", antiAliasingMaxSamples);
    }
    
    public static void main(final String[] args) {
        if (instance != null) throw new IllegalStateException("Client already running!");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    instance = new Client();
                    ArgumentParser.parse(args, new ArgumentParser.Argument(false, "--offline") {
                        @Override
                        public void runAction(String paramater) {
                            instance.offline = true;
                        }
                    }, new ArgumentParser.Argument(true, "--loglevel") {
                        @Override
                        public void runAction(String paramater) {
                            LogHelper.setLevel(Level.parse(paramater));
                        }
                    });
                    instance.start();
                } catch (Exception ex) {
                    try {
                        crashException(ex);
                    }
                    catch (Exception ex2) {
                        System.exit(0); // Everything broke, bail!
                    }
                }
            }
        }, "Client Thread").start();
    }

    public static Client getInstance() {
        return instance;
    }
    
    public void start() throws LWJGLException, SlickException, IOException {
        if (running) throw new IllegalStateException("Client already running!");
        running = true;
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        initDisplayModes();
        initConfig();
        
        Display.setDisplayMode(displayMode);

        Display.create();
        makeFrame();

        textureManager = new TextureManager();
        init();
        drawSplash();
        Display.update();

        Controllers.create();
        String defaultController = "";
        for (int i = 0; i < Controllers.getControllerCount(); i++) {
            Controller con = Controllers.getController(i);
            if (con.getAxisCount() >= 2) {
                validControllers.put(con.getName(), i);
                config.defaultProperty("controls.controller", con.getName());
                if (defaultController.isEmpty()) defaultController = con.getName();
                LogHelper.config("Found controller: %s (%d Rumblers)", con.getName(), con.getRumblerCount());
            }
        }
        if (validControllers.size() < 1) config.setProperty("controls.controller", "");
        else if (!validControllers.containsKey(config.getString("controls.controller"))) config.setProperty("controls.controller", defaultController);
        if (config.hasChanged()) config.save();
        
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
        System.exit(0);
    }

    private void makeFrame() throws LWJGLException {
        if (frame != null) frame.dispose();
        frame = new JFrame(Constants.GAME_TITLE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        frame.setAlwaysOnTop(false);
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
        resizeFrame(false);
    }

    private void resizeFrame(boolean fullscreen) throws LWJGLException {
        Display.setParent(null);
        frame.dispose();
        frame.setUndecorated(fullscreen);
        if (fullscreen) GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
        else GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
        //frame.setPreferredSize(new java.awt.Dimension(displayMode.getWidth(), displayMode.getHeight()));
        canvas.setPreferredSize(new java.awt.Dimension(displayMode.getWidth(), displayMode.getHeight()));
        frame.pack();
        java.awt.Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((dim.width - frame.getSize().width) / 2, (dim.height - frame.getSize().height) / 2);
        frame.setVisible(true);
        Display.setParent(canvas);
    }

    public static void crashException(Throwable ex) {
        ex.printStackTrace();
        if (instance != null) {
            try {
                if (instance.frame != null) {
                    instance.frame.setResizable(false);
                    instance.frame.setSize(new java.awt.Dimension(instance.displayMode.getWidth(), instance.displayMode.getHeight()));
                    // todo
                    while (!instance.closeRequested) {
                        try { Thread.sleep(100); }
                        catch (InterruptedException junk) { }
                    }
                }
                instance.shutdownInternal();
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
        config.defaultProperty("display.antialiasing", true);
        config.defaultProperty("display.antialiasingsamples", 4);
        //config.defaultProperty("display.fullscreen", false);
        config.defaultProperty("sound.effectvolume", 1.0F);
        config.defaultProperty("sound.musicvolume", 1.0F);

        if (!internalSetDisplayMode(config.getInteger("display.width"), config.getInteger("display.height"))) {
            config.setProperty("display.width", displayMode.getWidth());
            config.setProperty("display.height", displayMode.getHeight());
        }
        antiAliasing = config.getBoolean("display.antialiasing");
        antiAliasingSamples = config.getInteger("display.antialiasingsamples");
        //fullscreen = config.getBoolean("display.fullscreen");

        if (!antiAliasingSupported) {
            antiAliasing = false;
            config.setProperty("display.antialiasing", false);
        } else if (antiAliasingSamples < 2 || antiAliasingSamples > antiAliasingMaxSamples || !Util.isPowerOfTwo(antiAliasingSamples)) {
            antiAliasingSamples = 4;
            config.setProperty("display.antialiasingsamples", 4);
        }

        if (config.getInteger("version") < Constants.VERSION) {
            config.setProperty("version", Constants.VERSION);
        }
        
        if (config.hasChanged()) config.save();
    }

    public DisplayMode findDisplayMode(int width, int height) {
        for (DisplayMode mode : displayModeList) {
            if(mode.getWidth() == width && mode.getHeight() == height) {
                return mode;
            }
        }
        return null;
    }

    public void useDisplayMode() throws LWJGLException {
        try {
            DisplayMode desktopMode = Display.getDesktopDisplayMode();
            if (fullscreen) {
                if (!desktopMode.equals(displayMode)) displayMode = desktopMode;
            } else displayMode = configDisplayMode;
            resizeFrame(fullscreen);
            Display.setDisplayMode(displayMode);
            resizeGL(displayMode.getWidth(), displayMode.getHeight());
            setupAntiAliasing();
            for (GUICallback callback : resizeHandlers) {
                callback.run();
            }
        }
        catch (LWJGLException ex) {
            crashException(ex);
            shutdownInternal();
        }
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.newDisplayMode = displayMode;
    }

    public boolean setDisplayMode(int width, int height) {
        DisplayMode mode = findDisplayMode(width, height);
        if (mode != null) {
            setDisplayMode(mode);
            return true;
        }
        return false;
    }

    private boolean internalSetDisplayMode(int width, int height) {
        DisplayMode mode = findDisplayMode(width, height);
        if (mode != null) {
            displayMode = mode;
            configDisplayMode = mode;
            return true;
        }
        return false;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.newFullscreen = fullscreen;
    }

    private void setupAntiAliasing() {
        if (multisampleFBO != 0) {
            glDeleteTextures(multisampleTexture);
            glDeleteFramebuffers(multisampleFBO);
            multisampleTexture = 0;
            multisampleFBO = 0;
        }
        if (antiAliasing) {
            multisampleTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, multisampleTexture);
            glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, antiAliasingSamples, GL_RGBA8, displayMode.getWidth(), displayMode.getHeight(), false);
            multisampleFBO = glGenFramebuffers();
            glBindFramebuffer(GL_FRAMEBUFFER, multisampleFBO);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, multisampleTexture, 0);
            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Anti-aliasing framebuffer is invalid.");
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
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

    public boolean removeResizeHandler(GUICallback resizeHandler) {
        return resizeHandlers.remove(resizeHandler);
    }

    public GUICallback removeResizeHandler(int index) {
        return resizeHandlers.remove(index);
    }

    public void clearResizeHandlers() {
        resizeHandlers.clear();
    }

    public boolean addPreProcesor(Runnable processor) {
        return preProcessors.add(processor);
    }

    public boolean removePreProcesor(Runnable processor) {
        return preProcessors.remove(processor);
    }

    public boolean addPostProcesor(Runnable processor) {
        return postProcessors.add(processor);
    }

    public boolean removePostProcesor(Runnable processor) {
        return postProcessors.remove(processor);
    }

    private void preloadData() {
        // Preload Textures
        textureManager.getTexture("ui/windowclose.png");
        
        // Preload Sounds
        soundManager.loadSound("ui/click.wav");
        soundManager.loadSound("ui/rollover.wav");
    }
    
    private void drawBackground() {
        drawSplash();
    }

    private void drawSplash() {
        textureManager.getTexture("background.png").bind();
        RenderHelper.drawSquare(0, 0, displayMode.getWidth(), displayMode.getHeight(), true);
    }

    private void renderResourceProgress() {
        drawSplash();
        UnicodeFont theFont = fontManager.getFont("batmfa_", 50, false, false).getUnicodeFont();
        int width = theFont.getWidth(ResourceDownloader.getStatus());
        Vector2 fontCenter = getScreenCenter(width, 0);
        theFont.drawString(fontCenter.getX(), fontCenter.getY() - 60, ResourceDownloader.getStatus(), org.newdawn.slick.Color.white);
        float barWidth = 600;
        Vector2 barCenter = getScreenCenter((int)barWidth, 0);
        RenderHelper.drawSquare(barCenter.getX(), barCenter.getY(), barWidth, 30, new Color(50, 50, 50));
        RenderHelper.drawSquare(barCenter.getX(), barCenter.getY(), barWidth * ResourceDownloader.getProgress(), 30, new Color(0, 100, 0));
    }

    private void initIntro() {
        this.addScreen(new ScreenIntro());
    }
    
    private void init() {
        initGL();
        resizeGL(displayMode.getWidth(), displayMode.getHeight());
        setupAntiAliasing();
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

    public void resizeGL(int width, int height) {
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
                if (screen.isVisible() && screen.isEnabled() && !screen.processKeyboardEvent()) continue toploop;
            if (world != null && !world.processKeyboardEvent()) continue;
            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_F11) setFullscreen(!fullscreen);
        }
    }

    private void processMouse() {
        toploop: while (Mouse.next()) {
            for (Screen screen : screenList)
                if (screen.isVisible() && screen.isEnabled() && !screen.processMouseEvent()) continue toploop;
            if (world != null && !world.processMouseEvent()) continue;
            //if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && !asteroids.containsKey(getMousePos())) asteroids.put(getMousePos(), AsteroidGenerator.generate());
        }
    }

    private void processController() {
        toploop: while (Controllers.next()) {
            Controller con = Controllers.getEventSource();
            if (con.getName().equals(config.getString("controls.controller"))) {
                for (Screen screen : screenList)
                    if (screen.isVisible() && screen.isEnabled() && !screen.processControllerEvent(con)) continue toploop;
                if (world != null && !world.processControllerEvent(con)) continue;
            }
        }
    }

    private void update() {
        float delta = getDelta();

        Iterator<Screen> it = screenList.iterator();
        while (it.hasNext()) {
            Screen screen = it.next();
            if (screen.isRemoveRequested()) it.remove();
            else if (screen.isVisible() && screen.isEnabled()) screen.update(delta);
        }

        for (ScreenHolder holder : screensToAdd) {
            if (holder.getIndex() < 0) {
                screenList.add(holder.getScreen());
            } else {
                screenList.add(holder.getIndex(), holder.getScreen());
            }
        }
        screensToAdd.clear();

        if (world != null) world.update(delta);
    }

    private void render() {
        checkGLError("Pre render");
        if (antiAliasing) glBindFramebuffer(GL_DRAW_FRAMEBUFFER, multisampleFBO);
        long time = System.nanoTime();
        glClear(GL_COLOR_BUFFER_BIT);
        glLoadIdentity();
        
        if (renderBackground) drawBackground();
        if (!resourcesDone) { // We're gonna be a bit dirty here and do "update" code in the render method, makes things easier.
            renderResourceProgress();
            if (ResourceDownloader.isCompleted()) {
                resourcesDone = true;
                preloadData();
                initIntro();
            }
        }

        if (world != null) world.render();
        for (Screen screen : screenList)
            if (screen.isVisible()) screen.render();
        
        long renderTime = System.nanoTime() - time;
        if (renderDebug) {
            Runtime runtime = Runtime.getRuntime();
            UnicodeFont debugFont = fontManager.getFont("batmfa_", 20, false, false).getUnicodeFont();
            debugFont.drawString(5, 5, "FPS: " + fpsRender, org.newdawn.slick.Color.yellow);
            debugFont.drawString(5, 25, "Memory: " + Util.bytesToMBString(runtime.totalMemory() - runtime.freeMemory()) + " / " + Util.bytesToMBString(runtime.maxMemory()), org.newdawn.slick.Color.yellow);
            debugFont.drawString(5, 45, "Render time: " + (renderTime / 1000000D), org.newdawn.slick.Color.yellow);
            debugFont.drawString(5, 65, "Entities: " + (world != null ? world.getEntityCount() : 0), org.newdawn.slick.Color.yellow);
        }
        
        if (antiAliasing) {
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
            glBindFramebuffer(GL_READ_FRAMEBUFFER, multisampleFBO);
            glBlitFramebuffer(0, 0, displayMode.getWidth(), displayMode.getHeight(), 0, 0, displayMode.getWidth(), displayMode.getHeight(), GL_COLOR_BUFFER_BIT, GL_NEAREST);
        }
        checkGLError("Post render");
    }

    private void postProcess() {
        for (Runnable runnable : postProcessors) {
            runnable.run();
        }
    }

    private void checkGLError(String stage) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            LogHelper.severe("########## GL ERROR ##########");
            LogHelper.severe("@ %s", stage);
            LogHelper.severe("%d: %s", error, gluErrorString(error));
        }
    }

    private void run() throws LWJGLException {
        timeCounter = Sys.getTime();
        while(!Display.isCloseRequested() && !closeRequested) {
            if (!hasCrashed) {
                try {
                    runGameLoop();
                } catch (Exception ex) {

                }
            } else {
                closeRequested = true;
            }
        }
        shutdownInternal();
    }
    
    private void runGameLoop() throws LWJGLException {
        if (fullscreen && !frame.isFocused()) setFullscreen(false);
        if (newDisplayMode != null || newFullscreen != fullscreen) {
            if (newDisplayMode != null) {
                displayMode = newDisplayMode;
                configDisplayMode = newDisplayMode;
                config.setProperty("display.width", configDisplayMode.getWidth());
                config.setProperty("display.height", configDisplayMode.getHeight());
            }
            fullscreen = newFullscreen;
            newDisplayMode = null;
            useDisplayMode();
        }

        if (getTime() - timeCounter >= 1000) {
            fpsRender = fpsCounter;
            fpsCounter = 0;
            timeCounter += 1000;
        }
        fpsCounter++;

        mouseHitbox.setLocation(getMouseX(), getMouseY());
        soundManager.update();
        this.preProcess();
        this.processKeyboard();
        this.processMouse();
        this.processController();
        this.update();
        this.render();
        this.postProcess();

        Display.update();
        Thread.yield();
    }

    private long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    private float getDelta() {
        long time = System.nanoTime();
        float delta = (time - deltaTime) / 1000000000F;
        deltaTime = time;
        return delta;
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
        Integer index = validControllers.get(name);
        return index != null ? Controllers.getController(index) : null;
    }

    /*public Vector2f getViewportScale() {
        return new Vector2f((float)canvas.getWidth() / (float)displayMode.getWidth(), (float)canvas.getHeight() / (float)displayMode.getHeight());
    }*/

    public Vector2 getScreenCenter(Dimension dim) {
        return new Vector2((displayMode.getWidth() / 2f) - (dim.getWidth() / 2f), (displayMode.getHeight() / 2f) - (dim.getHeight() / 2f));
    }

    public Vector2 getScreenCenter(int width, int height) {
        return getScreenCenter(new Dimension(width, height));
    }

    public Vector2 getScreenCenter() {
        return getScreenCenter(new Dimension());
    }

    public Controller getActiveController() {
        return getController(config.getString("controls.controller"));
    }
    
    public int getMouseX() {
        //return (int)(Mouse.getX() / ((double)canvas.getWidth() / (double)displayMode.getWidth()));
        return (int)(Mouse.getX());
    }
    
    public int getMouseY() {
        //return (int)((canvas.getHeight() - Mouse.getY()) / ((double)canvas.getHeight() / (double)displayMode.getHeight()));
        return (int)(displayMode.getHeight() - Mouse.getY());
    }
    
    public Vector2 getMousePos() {
        return new Vector2(getMouseX(), getMouseY());
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
    
    public void setMousePos(Vector2 pos) {
        setMousePos((int)pos.getX(), (int)pos.getY());
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
        screensToAdd.add(new ScreenHolder(-1, screen));
    }

    public void addScreen(int index, Screen screen) {
        screensToAdd.add(new ScreenHolder(index, screen));
    }

    public boolean removeScreen(Screen screen) {
        boolean ret = screenList.remove(screen);
        if (ret) screen.remove();
        return ret;
    }

    public Screen removeScreen(int index) {
        Screen screen = screenList.get(index);
        screen.remove();
        return screen;
    }

    public void clearScreens() {
        for (Screen screen : screenList)
            screen.remove();
        screenList.clear();
    }

    public long getFPS() {
        return fpsRender;
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
    
    private void shutdownInternal() throws LWJGLException {
        running = false;
        if (config != null && config.hasChanged()) config.save();
        if (soundManager != null) soundManager.getSoundSystem().cleanup();
        if (textureManager != null) textureManager.cleanup();
        if (fontManager != null) fontManager.cleanup();
        Keyboard.destroy();
        Mouse.destroy();
        Display.destroy(); // Why the fuck is this causing a segmentation fault!?
    }
    
    public File getDataDirectory() {
        return dataDir;
    }

    @Value private class ScreenHolder {
        private int index;
        private Screen screen;
    }
}
