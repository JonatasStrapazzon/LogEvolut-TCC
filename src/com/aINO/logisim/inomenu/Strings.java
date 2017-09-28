package com.aINO.logisim.inomenu;

import com.cburch.logisim.util.LocaleManager;
import com.cburch.logisim.util.StringGetter;

public class Strings {
	public static String get(String key) {
		return source.get(key);
	}

	public static StringGetter getter(String key) {
		return source.getter(key);
	}

	private static LocaleManager source = new LocaleManager(
			"resources/logisim", "menu");

}
