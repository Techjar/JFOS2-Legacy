
package com.techjar.jfos2.util.logging;

import com.techjar.jfos2.util.Constants;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author Techjar
 */
public final class LogHelper {
    public static final Logger logger;
    public static final PrintStream realSystemOut;
    public static final PrintStream realSystemErr;

    static {
        realSystemOut = System.out;
        realSystemErr = System.err;
        logger = Logger.getLogger("JFOS2");
        logger.setLevel(Level.CONFIG);
        try {
            File logDir = new File(Constants.DATA_DIRECTORY, "logs");
            logDir.mkdirs();
            logger.addHandler(new LogHandler(new File(logDir, new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()) + ".txt")).setSystemOut(System.out));
            logger.setUseParentHandlers(false);
            System.setOut(new PrintStream(new LogOutputStream(logger, Level.INFO), true));
            System.setErr(new PrintStream(new LogOutputStream(logger, Level.SEVERE), true));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public static void init() {} // Dummy method

    public static void setLevel(Level level) {
        logger.setLevel(level);
    }

    public static void error(String message, Throwable error) {
        logger.log(Level.SEVERE, message, error);
    }

    public static void severe(String message, Object... params) {
        logger.log(Level.SEVERE, String.format(message, params));
    }

    public static void warning(String message, Object... params) {
        logger.log(Level.WARNING, String.format(message, params));
    }

    public static void info(String message, Object... params) {
        logger.log(Level.INFO, String.format(message, params));
    }

    public static void config(String message, Object... params) {
        logger.log(Level.CONFIG, String.format(message, params));
    }

    public static void fine(String message, Object... params) {
        logger.log(Level.FINE, String.format(message, params));
    }

    public static void finer(String message, Object... params) {
        logger.log(Level.FINER, String.format(message, params));
    }

    public static void finest(String message, Object... params) {
        logger.log(Level.FINEST, String.format(message, params));
    }
}
