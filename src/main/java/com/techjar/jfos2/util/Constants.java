package com.techjar.jfos2.util;

import java.io.File;

/**
 * @author Techjar
 */
public final class Constants {
	private Constants() {
	}

	public static final String GAME_TITLE = "Junk from Outer Space 2";
	public static final File DATA_DIRECTORY = OperatingSystem.getDataDirectory("jfos2");
	public static final int DEFAULT_PORT = 17725;
	public static final int TICK_RATE = 60;
	public static final int VERSION = 1;
}
